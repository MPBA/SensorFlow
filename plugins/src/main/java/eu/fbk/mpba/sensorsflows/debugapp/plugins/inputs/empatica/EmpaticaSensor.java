package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.empatica;

import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class EmpaticaSensor extends SensorComponent<Long, double[]> {

    public EmpaticaSensor(DevicePlugin<Long, double[]> p) {
        super(p);
    }

    @Override
    public void switchOnAsync() {

    }

    @Override
    public void switchOffAsync() {

    }

    @Override
    public List<Object> getValuesDescriptors() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }
}