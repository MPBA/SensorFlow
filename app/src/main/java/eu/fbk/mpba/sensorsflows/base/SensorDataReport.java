package eu.fbk.mpba.sensorsflows.base;
import eu.fbk.mpba.sensorsflows.SensorImpl;

public class SensorDataReport<TimeT, ValueT> {
    public SensorImpl<ValueT, TimeT> sensor;
    public TimeT code;
    public ValueT message;
}
