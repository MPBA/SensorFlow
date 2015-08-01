package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.android;

import android.content.Context;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.TextEventsSensor;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class SmartphoneDevice implements DevicePlugin<Long, double[]>, IMonotonicTimestampReference {

    private String name;
    private List<SensorComponent<Long, double[]>> _sensors;
    private TextEventsSensor<double[]> _textSensor;

    public SmartphoneDevice(Context context, String name) {
        this(context, name, true, true, true);
    }

    public SmartphoneDevice(Context context, String name, boolean accelerometer, boolean gps, boolean text) {
        this.name = name;
        setBootUTCNanos();
        _sensors = new ArrayList<>();
        if (gps)
            _sensors.add(new GpsSensor(this, context, "0", 0, 0));
        if (accelerometer)
            _sensors.add(new AccelerometerSensor(this, context, "0", SensorManager.SENSOR_DELAY_FASTEST));
        if (text)
            _sensors.add(_textSensor = new TextEventsSensor<>(this, "0"));
    }

    public void addNoteNow(String text) {
        _textSensor.addText(text);
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

    private long refUTCNanos;

    public void setBootUTCNanos() {
        refUTCNanos = System.currentTimeMillis() * 1000000 - System.nanoTime();
    }

    public long getMonoUTCNanos(long realTimeNanos) {
        return realTimeNanos + refUTCNanos;
    }

    @Override
    public String getName() {
        return name;
    }
}

