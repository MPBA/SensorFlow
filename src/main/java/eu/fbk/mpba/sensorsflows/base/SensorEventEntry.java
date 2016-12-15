package eu.fbk.mpba.sensorsflows.base;

public class SensorEventEntry {
    public SensorEventEntry(ISensor flow, long timestamp, int code, String message){
        this.flow = flow;
        this.timestamp = timestamp;
        this.code = code;
        this.message = message;
    }

    public ISensor flow;
    public long timestamp;
    public int code;
    public String message;
}
