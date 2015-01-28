package eu.fbk.mpba.sensorsflowsa.stubs;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflowsa.DevicePlugIn;
import eu.fbk.mpba.sensorsflowsa.SensorComponent;
import eu.fbk.mpba.sensorsflowsa.util.ReadOnlyIterable;

/**
 * Test device with two basic sensors.
 */
public class TestDevicePlugIn extends DevicePlugIn<Long, float[]> {

    private String _name;
    private List<SensorComponent<Long, float[]>> _sensors;

    public TestDevicePlugIn(String name) {
        _name = name;
        _sensors = new ArrayList<SensorComponent<Long, float[]>>();
        _sensors.add(new RandomSensorStub(this));
        _sensors.add(new SequentialSensorStub(this));
    }

    @Override
    public Iterable<SensorComponent<Long, float[]>> getSensors() {
        return new ReadOnlyIterable<SensorComponent<Long, float[]>>(_sensors.iterator());
    }

    @Override
    protected void pluginInitialize() {
        for (SensorComponent<Long, float[]> s : _sensors) {
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
