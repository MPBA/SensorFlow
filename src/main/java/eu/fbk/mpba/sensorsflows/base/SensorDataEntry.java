package eu.fbk.mpba.sensorsflows.base;

public class SensorDataEntry {
    public SensorDataEntry(ISensor sensor, long timestamp, double[] value) {
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.value = value;
    }

    public ISensor sensor;
    public long timestamp;
    public double[] value;
}
