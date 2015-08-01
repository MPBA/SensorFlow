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
    private TimeOffsetSensor _timeOffsetSensor;

    public SmartphoneDevice(Context context, String name) {
        this.name = name;
        setBootUTCNanos();
        _sensors = new ArrayList<>();
        _sensors.add(new GpsSensor(this, context, "0", 0, 0));
        _sensors.add(new AccelerometerSensor(this, context, "0", SensorManager.SENSOR_DELAY_FASTEST));
        _sensors.add(_textSensor = new TextEventsSensor<>(this, "0"));
        _sensors.add(_timeOffsetSensor = new TimeOffsetSensor(this, "0"));
    }

    public void addNoteNow(String text) {
        _textSensor.addText(text);
    }

    public void computeOffsetBroadcastedAsync(int passes, LanUdpTimeClient.TimeOffsetCallback cb) {
        _timeOffsetSensor.computeOnEveryServer(passes, cb);
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

