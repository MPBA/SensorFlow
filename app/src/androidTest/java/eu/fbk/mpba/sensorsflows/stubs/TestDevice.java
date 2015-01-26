package eu.fbk.mpba.sensorsflows.stubs;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DeviceImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * Test device with two basic sensors.
 */
public class TestDevice extends DeviceImpl<Long, float[]> {

    private String _name;
    private List<SensorImpl<Long, float[]>> _sensors;

    public TestDevice(String name) {
        _name = name;
        _sensors = new ArrayList<SensorImpl<Long, float[]>>();
        _sensors.add(new RandomSensorStub(this));
        _sensors.add(new SequentialSensorStub(this));
    }

    @Override
    public Iterable<SensorImpl<Long, float[]>> getSensors() {
        return new ReadOnlyIterable<SensorImpl<Long, float[]>>(_sensors.iterator());
    }

    @Override
    protected void pluginInitialize() {
        for (SensorImpl<Long, float[]> s : _sensors) {
            s.switchOnAsync();
        }
    }

    @Override
    protected void pluginFinalize() {

    }

    @Override
    public String toString() {
        return "Device:" + _name;
    }
}
