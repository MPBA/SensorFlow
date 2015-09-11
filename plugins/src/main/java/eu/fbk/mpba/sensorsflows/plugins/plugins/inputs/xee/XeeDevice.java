package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class XeeDevice implements DevicePlugin<Long, double[]> {
    @Override
    public void inputPluginInitialize() {

    }

    @Override
    public void inputPluginFinalize() {

    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
