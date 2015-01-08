package eu.fbk.mpba.sensorsflows.base;

public class SensorEventEntry {
    public SensorEventEntry(ISensor sensor, int code, String message){
        this.sensor = sensor;
        this.code = code;
        this.message = message;
    }

    public ISensor sensor;
    public int code;
    public String message;
}
