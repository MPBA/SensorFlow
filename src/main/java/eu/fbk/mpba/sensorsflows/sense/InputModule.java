package eu.fbk.mpba.sensorsflows.sense;

/**
 * Base class for an InputModule
 */
public abstract class InputModule extends Module {

    final InputGroupImpl thisModule;

    @Override
    public boolean isFlowing() {
        return thisModule.flowing;
    }

    /**
     * Constructor of abstract class
     * @param name Name of the Module.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public InputModule(String name, String settings) {
        super(name, settings);
        thisModule = new InputGroupImpl(name) {
            @Override
            public void start() {
                InputModule.this.start();
            }

            @Override
            public void stop() {
                InputModule.this.stop();
            }
        };
        addSFChild(thisModule);
    }

    /**
     * Adds a Stream to the InputModule. The Input must have an unique name within the device inputs.
     * A Input can be added to the WirelessDevice scheme only when it is not flowing (isFlowing).
     * Special Flows are already present such as BatteryETA, BatterySOC, DataLoss, ConnectionStatus
     * and InternalErrors (getChildren).
     *
     * turnOn and turnOff are automatically called on start and stop.
     *
     * @param input The flow to add to the InputModule.
     */
    protected void addStream(Stream input) {
        if (isFlowing()) {
            thisModule.addChild(input);
        } else
            throw new UnsupportedOperationException("Can't alter inputs during acquisition.");
    }

    /**
     * This method is called when the acquisition is starting. After this method returns, data can
     * be pushed, and the method isFlowing returns true.
     */
    protected abstract void start();

}
