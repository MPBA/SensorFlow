package eu.fbk.mpba.sensorsflows.base;

public class SensorEventEntry<TimeT, ValueT> {
    public SensorEventEntry(ISensor sensor, TimeT timestamp, ValueT code, String message){
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.code = code;
        this.message = message;
    }

    public ISensor sensor;
    public TimeT timestamp;
    public ValueT code;
    public String message;
}
