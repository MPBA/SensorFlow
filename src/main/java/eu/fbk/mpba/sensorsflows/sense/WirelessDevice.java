package eu.fbk.mpba.sensorsflows.sense;


/**
 * WirelessDevice enables by extension interacting with devices that have a battery, a wireless
 * connection and may lose data.
 * 
 * To notify such data, methods of the form protected boolean onX() are provided. These control
 * internal Flows that send the data in a standard form.
 * 
 */
public abstract class WirelessDevice extends InputModule {
    public WirelessDevice(String settings) {
        super(settings);
    }

    // Control of built-in flows

    /**
     * Notifies a change in the remaining battery time (in seconds). Should be called at least once to notify that
     * the device has a battery.
     * @param seconds
     */
    protected void onBatteryETA(double seconds) {
        batteryETA.pushValue(seconds);
    }

    /**
     * Notifies a change in the battery State Of Charge (SOC %). Should be called at least once to notify that
     * the device has a battery.
     * @param percentage Value from 0 (Empty) to 100 Fully charged.
     */
    protected void onBatterySOC(double percentage) {
        batterySOC.pushValue(percentage);
    }

    /**
     * Notifies a change in the status of the datalink connection. (Standard not yet defined).
     * @param status
     */
    protected void onConnectionStatus(String status) {
        connection.pushLog(status);
    }

    /**
     * Notifies a change in the physical wireless connection strength in percentage.
     * The reference for the value is 0 (Unable to connect) to 100 (Excellent).
     * @param percentage
     */
    protected void onConnectionStrength(double percentage) {
        connection.pushValue(percentage);
    }

    /**
     * Notifies a detected loss of data in the connection.
     * @param bytes
     */
    protected void onDataLoss(double bytes) {
        dataLoss.pushValue(bytes);
    }

    // OOP stuff

    private Stream batteryETA = new Stream(this, "time", "BatteryETA");
    private Stream batterySOC = new Stream(this, "percentage", "BatterySOC");
    private Stream connection = new Stream(this, "percentage", "Connection");
    private Stream dataLoss = new Stream(this, "bytes", "DataLoss");
    {
        addFlow(batteryETA);
        addFlow(batterySOC);
        addFlow(connection);
        addFlow(dataLoss);
    }
}
