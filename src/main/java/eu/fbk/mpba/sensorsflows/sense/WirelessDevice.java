package eu.fbk.mpba.sensorsflows.sense;

import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.fbk.mpba.sensorsflows.Flow;
import eu.fbk.mpba.sensorsflows.Input;

public abstract class WirelessDevice implements Input {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public void onInputStart() {
        flowing = true;
    }

    @Override
    public void onInputStop() {
        flowing = false;
    }

    @Override
    public Iterable<Flow> getFlows() {
        return flows.values();
    }

    private boolean flowing = false;

    private TreeMap<String, Flow> flows = new TreeMap<>();

    /**
     * Returns weather the current device has been started and should be flowing data.
     *
     * @return True if it is flowing, else false.
     */
    public boolean isFlowing() {
        return flowing;
    }

    /**
     * Adds a flow to the WirelessDevice. The Flow must have an unique name within the device flows.
     * A Flow can be added to the WirelessDevice scheme only when it is not flowing (isFlowing).
     * Special Flows are already present such as BatteryETA, BatterySOC, DataLoss, ConnectionStatus
     * and InternalErrors (getFlows).
     *
     * @param flow The flow to add to the Device.
     */
    protected void addFlow(Flow flow) {
        flows.put(flow.getName(), flow);
    }

    protected WirelessDevice() {

    }

    // Control of built-in flows

    /**
     *
     * @param seconds
     * @return
     */
    protected boolean onBatteryETA(double seconds) {
        return batteryETA.onValue(seconds);
    }

    /**
     *
     * @param percentage
     * @return
     */
    protected boolean onBatterySOC(double percentage) {
        return batterySOC.onValue(percentage);
    }

    /**
     *
     * @param status
     * @return
     */
    protected boolean onConnectionStatus(String status) {
        return connectionStatusLog.onLog(status);
    }

    /**
     *
     * @param percentage
     * @return
     */
    protected boolean onConnectionStrength(double percentage) {
        return connectionStrength.onValue(percentage);
    }

    /**
     *
     * @param bytes
     * @return
     */
    protected boolean onDataLoss(double bytes) {
        return dataLoss.onValue(bytes);
    }

    /**
     *
     * @param gravity
     * @param code
     * @param message
     * @return
     */
    protected boolean onDeviceLog(Level gravity, int code, String message) {
        return deviceLog.onLog(String.format(Locale.ENGLISH, "[%d] %d %s", gravity.intValue(), code, message));
    }

    // OOP junk

    private PassiveFlow batteryETA = new PassiveFlow(this, "time", "BatteryETA");
    private PassiveFlow batterySOC = new PassiveFlow(this, "percentage", "BatterySOC");
    private PassiveFlow connectionStatusLog = new PassiveFlow(this, "", "ConnectionStatus");
    private PassiveFlow connectionStrength = new PassiveFlow(this, "rssi", "ConnectionStrength");
    private PassiveFlow dataLoss = new PassiveFlow(this, "bytes", "DataLoss");
    private PassiveFlow deviceLog = new PassiveFlow(this, "", "DeviceLog");
}
