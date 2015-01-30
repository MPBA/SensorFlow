package eu.fbk.mpba.sensorsflows.gpspluginapp.plugins;

import android.content.Context;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugIn;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class SmartphoneDevice extends DevicePlugIn<Long, double[]> implements IMonotonicTimestampReference {

    private String name;
    private List<SensorComponent<Long, double[]>> _sensors;

    public SmartphoneDevice(Context context, String name) {
        this.name = name;
        resetMonoTimestamp(System.currentTimeMillis(), System.nanoTime());
        _sensors = new ArrayList<>();
        _sensors.add(new GpsSensor(this, context, "0", 0, 0));
        _sensors.add(new AccelerometerSensor(this, context, "1-noSafeTS", SensorManager.SENSOR_DELAY_GAME));
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(_sensors.iterator());
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

    private long bootUTCNanos;

    @Override
    public void resetMonoTimestamp(long timestamp, long realTimeNanos) {
        bootUTCNanos = timestamp * 1000000 - realTimeNanos;
    }

    @Override
    public long getMonoTimestampNanos(long realTimeNanos) {
        return realTimeNanos + bootUTCNanos;
    }
}

