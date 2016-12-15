package eu.fbk.mpba.sensorsflows.base;

public class SensorDataEntry {
    public SensorDataEntry(ISensor flow, long timestamp, double[] value) {
        this.flow = flow;
        this.timestamp = timestamp;
        this.value = value;
    }

    public ISensor flow;
    public long timestamp;
    public double[] value;
}
