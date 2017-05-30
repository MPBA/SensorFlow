package eu.fbk.mpba.sensorsflows.sense;

import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.fbk.mpba.sensorsflows.Flow;
import eu.fbk.mpba.sensorsflows.Input;

/**
 * WirelessDevice enables by extension interactining with devices that have a battery, a wireless connection and
 * can lose data.
 * 
 * To notify such data methods of the form protected boolean onX() are provided. These control internal Flows that
 * send the data in a standard form.
 * 
 */
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
     * Notifies a change in the remaining battery time (in seconds). Should be called at least once to notify that
     * the device has a battery.
     * @param seconds
     * @return Weather the current flow is ON
     */
    protected boolean onBatteryETA(double seconds) {
        return batteryETA.onValue(seconds);
    }

    /**
     * Notifies a change in the battery State Of Charge (SOC %). Should be called at least once to notify that
     * the device has a battery.
     * @param percentage Value from 0 (Empty) to 100 Fully charged.
     * @return Weather the current flow is ON
     */
    protected boolean onBatterySOC(double percentage) {
        return batterySOC.onValue(percentage);
    }

    /**
     * Notifies a change in the status of the datalink connection. (Standard not yet defined).
     * @param status
     * @return Weather the current flow is ON
     */
    protected boolean onConnectionStatus(String status) {
        return connectionStatusLog.onLog(status);
    }

    /**
     * Notifies a change in the physical wireless connection strength in percentage.
     * The reference for the value is 0 (Unable to connect) to 100 (Excellent).
     * @param percentage
     * @return Weather the current flow is ON
     */
    protected boolean onConnectionStrength(double percentage) {
        return connectionStrength.onValue(percentage);
    }

    /**
     * Notifies a detected loss of data in the connection.
     * @param bytes
     * @return Weather the current flow is ON
     */
    protected boolean onDataLoss(double bytes) {
        return dataLoss.onValue(bytes);
    }

    /**
     * Custom logs from the device software/hardware. Should contain information about the hardware and firmware
     * versions for reproducibiliti (TODO: Standard enumeration of codes)
     * @param gravity Gravity of the log, may be ignored
     * @param code Identification code of the log type
     * @param message String containing the log message
     * @return Weather the current flow is ON
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
