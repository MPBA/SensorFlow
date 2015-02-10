package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import android.content.Context;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class SmartphoneDevice implements DevicePlugin<Long, double[]>, IMonotonicTimestampReference {

    private String name;
    private List<SensorComponent<Long, double[]>> _sensors;

    public SmartphoneDevice(Context context, String name) {
        this.name = name;
        resetMonoTimestamp(System.currentTimeMillis(), System.nanoTime());
        _sensors = new ArrayList<>();
        _sensors.add(new GpsSensor(this, context, "0", 0, 0));
        _sensors.add(new AccelerometerSensor(this, context, "acc", SensorManager.SENSOR_DELAY_GAME));
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(_sensors.iterator());
    }

    @Override
    public void inputPluginInitialize() {
        for (SensorComponent<Long, double[]> s : _sensors) {
            s.switchOnAsync();
        }
    }

    @Override
    public void inputPluginFinalize() {
        for (SensorComponent<Long, double[]> s : _sensors) {
            s.switchOffAsync();
        }
    }

    private long bootUTCNanos;

    public void resetMonoTimestamp(long timestamp, long realTimeNanos) {
        bootUTCNanos = timestamp * 1000000 - realTimeNanos;
    }

    public long getMonoTimestampNanos(long realTimeNanos) {
        return realTimeNanos + bootUTCNanos;
    }

    @Override
    public String toString() {
        return name;
    }
}

