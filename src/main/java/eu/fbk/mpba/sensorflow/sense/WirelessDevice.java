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

    private static  ConnectionType connectionType = ConnectionType.OTHER;
    private String deviceName;

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
     * Notifies a change in the status of the datalink connection.
     */
    public void pushConnectionStatus(ConnectionStatus status) {
        connection.pushLog(status.name());
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
     * onConnectionStatus.
     */
    public abstract void connect(Object device);

    /**
     * Called by the developer who uses this module to disconnect the device. The output flows from
     * onConnectionStatus.
     */
    public void disconnect() { /* FIXME: abstract me */ }

    // OOP stuff

    private Stream batteryETA = new Stream(this, Stream.HEADER_VALUE, "battery-eta", true);
    private Stream batterySOC = new Stream(this, Stream.HEADER_VALUE, "battery-soc", true);
    private Stream connection = new Stream(this, Stream.HEADER_VALUE, "connection", true);
    private Stream dataLoss = new Stream(this, Stream.HEADER_VALUE, "data-loss");
    {
        addStream(batteryETA);
        addStream(batterySOC);
        addStream(connection);
        addStream(dataLoss);
    }

    public String getDeviceName() {
        return deviceName;
    }

    protected void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public enum ConnectionType {
        BLUETOOTH_2,
        BLUETOOTH_3,
        BLUETOOTH_4,
        WIFI,
        OTHER
    }

    public enum ConnectionStatus {
        ACTION_NOT_SUPPORTED,
        CONNECTED,
        CONNECTING,
        CONNECTION_DROPPED_NO_ACTION,
        CONNECTION_DROPPED_RECONNECTING,
        CONNECTION_FAILED,
        DESTINATION_NOT_AVAILABLE,
        DISCONNECTED,
        WRONG_DESTINATION_TYPE,
    }
}
