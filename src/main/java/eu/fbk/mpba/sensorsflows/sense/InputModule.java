package eu.fbk.mpba.sensorsflows.sense;

import java.util.TreeMap;

import eu.fbk.mpba.sensorsflows.Flow;
import eu.fbk.mpba.sensorsflows.Input;

/**
 * Base class for an InputModule
 */
public abstract class InputModule extends Plugin implements Input {

    private boolean flowing = false;
    private TreeMap<String, Flow> flows = new TreeMap<>();
    private Stream deviceLog = new Stream(this, "", "DeviceLog");

    /**
     * Constructor of abstract class
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public InputModule(String settings) {
        super(settings);
        addFlow(deviceLog);
    }

    @Override
    public final void onInputStart() {
        start();
        flowing = true;
    }

    @Override
    public final void onInputStop() {
        stop();
        flowing = false;
    }

    /**
     * This method is called to know about the Flows exposed by this Module.
     * @return An object to iterate on the Flows.
     */
    @Override
    public Iterable<Flow> getFlows() {
        return flows.values();
    }

    /**
     * True when the acquisition is in progress i.e. between the start and stop calls. When it
     * returns true, some attributes of the InputModule can not be altered.
     *
     * @return True if it is flowing, false otherwise.
     */
    @Override
    public boolean isFlowing() {
        return flowing;
    }

    /**
     * This method is called when the acquisition is starting. After this method returns, data can
     * be pushed, and the method isFlowing returns true.
     */
    protected abstract void start();

    /**
     * Adds a flow to the InputModule. The Flow must have an unique name within the device flows.
     * A Flow can be added to the WirelessDevice scheme only when it is not flowing (isFlowing).
     * Special Flows are already present such as BatteryETA, BatterySOC, DataLoss, ConnectionStatus
     * and InternalErrors (getFlows).
     *
     * @param flow The flow to add to the InputModule.
     */
    protected void addFlow(Flow flow) {
        if (isFlowing()) {
            flows.put(flow.getName(), flow);
        } else
            throw new UnsupportedOperationException("Can't alter flows during acquisition.");
    }

    /**
     * Custom logs from the device software/hardware. Should contain information about the hardware
     * and firmware versions for reproducibility (TODO: Standard enumeration of codes)
     * @param type Identification code of the log type
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    protected void onInputLog(int type, String tag, String message) {
        deviceLog.pushLog(type, tag, message);
    }
}
