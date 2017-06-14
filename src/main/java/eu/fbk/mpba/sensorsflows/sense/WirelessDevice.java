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
    public WirelessDevice(String name, String settings) {
        super(name, settings);
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

    private Stream batteryETA = new Stream(thisModule, "time", "BatteryETA");
    private Stream batterySOC = new Stream(thisModule, "percentage", "BatterySOC");
    private Stream connection = new Stream(thisModule, "percentage", "Connection");
    private Stream dataLoss = new Stream(thisModule, "bytes", "DataLoss");
    {
        addStream(batteryETA);
        addStream(batterySOC);
        addStream(connection);
        addStream(dataLoss);
    }
}
