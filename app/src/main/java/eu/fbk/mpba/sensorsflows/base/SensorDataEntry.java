package eu.fbk.mpba.sensorsflows.base;

public class SensorDataEntry<TimeT, ValueT> {
    public SensorDataEntry(ISensor sensor, TimeT time, ValueT value) {
        this.sensor = sensor;
        this.time = time;
        this.value = value;
    }

    public ISensor sensor;
    public TimeT time;
    public ValueT value;
}
