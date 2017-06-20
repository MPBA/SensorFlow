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
     * @param seconds   Estimated remaining time before power off due to low battery. This time,
     *                  despite being in seconds, has neither to change every second nor to be monotonic.
     */
    protected void onBatteryETA(double seconds) {
        batteryETA.pushValue(seconds);
    }

    /**
     * Notifies a change in the battery State Of Charge (SOC %). Should be called at least once to notify that
     * the device has a battery.
     * @param percentage Value from 0 (Empty) to 100 Fully charged. Neither granularity, nor
     *                   monotonicity requirements.
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
     * @param percentage    Percentage linearly indicating the quality or power of the connection
     *                      where 0% is the complete loss of connection and 100% is the maximal
     *                      power receivable.
     */
    protected void onConnectionStrength(double percentage) {
        connection.pushValue(percentage);
    }

    /**
     * Notifies a loss of data in the connection.
     * @param bytes Indicative quantity of information lost. If the data is retransmitted due to
     *              some Transmission Control mechanism it has not to be considered lost.
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
