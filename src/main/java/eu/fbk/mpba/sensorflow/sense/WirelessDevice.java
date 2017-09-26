package eu.fbk.mpba.sensorflow.sense;


/**
 * WirelessDevice enables by extension interacting with devices that have a battery, a wireless
 * connection and may lose data.
 * 
 * To notify such data, methods of the form protected boolean pushX() are provided. These control
 * internal Flows that send the data in a standard form.
 *
 * Remember to implement the following static method:
 * public static DeviceDetector getDeviceDetector()
 */
public abstract class WirelessDevice extends InputModule {

    private final ConnectionType connectionType;

    public WirelessDevice(String name, String settings, ConnectionType connectionType) {
        super(name, settings);
        this.connectionType = connectionType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    // Control of built-in flows

    /**
     * Notifies a change in the remaining battery time (in seconds). Should be called at least once to notify that
     * the device has a battery.
     * @param seconds   Estimated remaining time before power off due to low battery. This time,
     *                  despite being in seconds, has neither to change every second nor to be monotonic.
     */
    public void pushBatteryETA(double seconds) {
        batteryETA.pushValue(seconds);
    }

    /**
     * Notifies a change in the battery State Of Charge (SOC %). Should be called at least once to notify that
     * the device has a battery.
     * @param percentage Value from 0 (Empty) to 100 Fully charged. Neither granularity, nor
     *                   monotonicity requirements.
     */
    public void pushBatterySOC(double percentage) {
        batterySOC.pushValue(percentage);
    }

    public void pushBatterySOC(double volts, double fullVoltage, double emptyVoltage) {
        pushBatterySOC((volts - emptyVoltage) / (fullVoltage - emptyVoltage) * 100);
    }

    /**
     * Notifies a change in the status of the datalink connection. (Standard not yet defined).
     */
    public void pushConnectionStatus(String status) {
        connection.pushLog(0, "", status);
    }

    /**
     * Notifies a change in the physical wireless connection strength in percentage.
     * The reference for the value is 0 (Unable to connect) to 100 (Excellent).
     * @param percentage    Percentage linearly indicating the quality or power of the connection
     *                      where 0% is the complete loss of connection and 100% is the maximal
     *                      power receivable.
     */
    public void pushConnectionStrength(double percentage) {
        connection.pushValue(percentage);
    }

    /**
     * Notifies a loss of data in the connection.
     * @param bytes Indicative quantity of information lost. If the data is retransmitted due to
     *              some Transmission Control mechanism it has not to be considered lost.
     */
    public void pushDataLoss(double bytes) {
        dataLoss.pushValue(bytes);
    }

    /**
     * Called by the developer who uses this module to connect the device. The output flows from
     * onConnectionStatus, and is available through the getter getConnectionStatus.
     */
    public abstract void connect(Runnable done);

    // OOP stuff

    private Stream batteryETA = new Stream(this, Stream.HEADER_VALUE, "battery-eta");
    private Stream batterySOC = new Stream(this, Stream.HEADER_VALUE, "battery-soc");
    private Stream connection = new Stream(this, Stream.HEADER_VALUE, "connection-strength");
    private Stream dataLoss = new Stream(this, Stream.HEADER_VALUE, "data-loss");
    {
        addStream(batteryETA);
        addStream(batterySOC);
        addStream(connection);
        addStream(dataLoss);
    }

    public enum ConnectionType {
        BLUETOOTH_2,
        BLUETOOTH_3,
        BLUETOOTH_4,
        WIFI,
        OTHER
    }
}
