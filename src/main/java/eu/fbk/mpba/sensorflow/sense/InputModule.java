package eu.fbk.mpba.sensorflow.sense;

/**
 * Base class for an InputModule
 */
public abstract class InputModule extends Module {

    final InputGroupImpl thisModule;

    /**
     * Constructor of abstract class
     * @param name Name of the Module.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public InputModule(String name, String settings) {
        super(name, settings);
        thisModule = new InputGroupImpl(name) {
            @Override
            public synchronized void onCreate() {
                InputModule.this.onCreate();
            }

            @Override
            public synchronized void onStart() {
                InputModule.this.onStart();
            }

            @Override
            public synchronized void onStop() {
                InputModule.this.onStop();
            }

            @Override
            public synchronized void onClose() {
                InputModule.this.onClose();
            }
        };
        addSFChild(thisModule);
    }

    /**
     * Adds a Stream to the InputModule. The Input must have an unique name within the device inputs.
     * An Input can be added to the WirelessDevice scheme in any moment.
     * Special Inputs are already present such as BatteryETA, BatterySOC, DataLoss, ConnectionStatus
     * and InternalErrors (getChildren).
     *
     * @param input The flow to add to the InputModule.
     */
    protected void addStream(Stream input) {
        thisModule.addChild(input);
    }

    public abstract void onCreate();

    public abstract void onStart();

    public abstract void onStop();

    public abstract void onClose();
}
