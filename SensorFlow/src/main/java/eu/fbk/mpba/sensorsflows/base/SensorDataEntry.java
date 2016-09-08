package eu.fbk.mpba.sensorsflows.base;

public class SensorDataEntry<TimeT, ValueT> {
    public SensorDataEntry(ISensor sensor, TimeT timestamp, ValueT value) {
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.value = value;
    }

    public ISensor sensor;
    public TimeT timestamp;
    public ValueT value;
}
