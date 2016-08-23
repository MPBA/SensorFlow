package eu.fbk.mpba.sensorsflows.base;

public class SensorEventEntry<TimeT> {
    public SensorEventEntry(ISensor sensor, TimeT timestamp, int code, String message){
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.code = code;
        this.message = message;
    }

    public ISensor sensor;
    public TimeT timestamp;
    public int code;
    public String message;
}
