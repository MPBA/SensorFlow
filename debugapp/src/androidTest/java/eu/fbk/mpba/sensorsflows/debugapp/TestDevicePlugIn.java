package eu.fbk.mpba.sensorsflows.debugapp;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.debugapp.plugins.RandomSensorStub;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.SequentialSensorStub;
import eu.fbk.mpba.sensorsflows.DevicePlugIn;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * Test device with two basic sensors.
 */
@SuppressWarnings("UnusedDeclaration")
public class TestDevicePlugIn extends DevicePlugIn<Long, float[]> {

    private String _name;
    private List<SensorComponent<Long, float[]>> _sensors;

    public TestDevicePlugIn(String name) {
        _name = name;
        _sensors = new ArrayList<>();
        _sensors.add(new RandomSensorStub(this));
        _sensors.add(new SequentialSensorStub(this));
    }

    @Override
    public Iterable<SensorComponent<Long, float[]>> getSensors() {
        return new ReadOnlyIterable<>(_sensors.iterator());
    }

    @Override
    protected void inputPluginInitialize() {
        for (SensorComponent<Long, float[]> s : _sensors) {
            s.switchOnAsync();
        }
    }

    @Override
    protected void inputPluginFinalize() {

    }

    @Override
    public String toString() {
        return "Device:" + _name;
    }
}
