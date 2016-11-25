package eu.fbk.mpba.sensorsflows.base;

public class SensorEventEntry {
    public SensorEventEntry(ISensor sensor, long timestamp, int code, String message){
        this.sensor = sensor;
        this.timestamp = timestamp;
        this.code = code;
        this.message = message;
    }

    public ISensor sensor;
    public long timestamp;
    public int code;
    public String message;
}
