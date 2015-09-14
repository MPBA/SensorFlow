package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class XeeSensor extends SensorComponent<Long, double[]> {

    protected XeeSensor(DevicePlugin<Long, double[]> parent) {
        super(parent);
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

    public static class ACC extends XeeSensor {

        protected ACC(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }
    }

    public static class GPS extends XeeSensor {

        protected GPS(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }
    }

    public static class DATA extends XeeSensor {

        protected DATA(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }
    }
}
