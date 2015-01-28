package eu.fbk.mpba.sensorsflows.gpspluginapp;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugIn;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class SmartphoneDevice extends DevicePlugIn {

    private String name;
    private List<SensorComponent<Long, double[]>> _sensors;

    public SmartphoneDevice(Context context, String name) {
        this.name = name;
        _sensors = new ArrayList<SensorComponent<Long, double[]>>();
        _sensors.add(new GpsSensor(context, "0", 0, 0));
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<SensorComponent<Long, double[]>>(_sensors.iterator());
    }

    @Override
    protected void pluginInitialize() {
        for (SensorComponent<Long, double[]> s : _sensors) {
            s.switchOnAsync();
        }
    }

    @Override
    protected void pluginFinalize() {
        for (SensorComponent<Long, double[]> s : _sensors) {
            s.switchOffAsync();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
