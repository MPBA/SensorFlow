package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import com.dquid.xee.sdk.DQAccelerometerData;
import com.dquid.xee.sdk.DQData;
import com.dquid.xee.sdk.DQGpsData;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public abstract class XeeSensor extends SensorComponent<Long, double[]> {

    protected boolean streaming = true;

    public int EC_CONNECTION = 0;
    public int EC_META = 1;

    protected XeeSensor(DevicePlugin<Long, double[]> parent) {
        super(parent);
    }

    @Override
    public void switchOnAsync() {
        streaming = true;
    }

    @Override
    public void switchOffAsync() {
        streaming = false;
    }

    public static class XeeAccelerometer extends XeeSensor {
        protected XeeAccelerometer(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

        void sensorValue(DQAccelerometerData d) {
            sensorValue(d.timestamp, new double[]{d.x, d.y, d.z, d.nda});
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object)"x", "y", "z", "nda");
        }

    }

    public static class XeeGPS extends XeeSensor {
        protected XeeGPS(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

        void sensorValue(DQGpsData d) {
            sensorValue(d.timestamp, new double[]{ d.latitude, d.longitude, d.altitude, d.heading, d.latitude_direction, d.longitude_direction, d.satellites });
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object)"latitude", "longitude", "altitude", "heading", "latitude_direction", "longitude_direction", "satellites");
        }

    }

    public static class CarData extends XeeSensor {
        String name;

        protected CarData(DevicePlugin<Long, double[]> parent, String name) {
            super(parent);
            this.name = name;
        }

        void sensorValue(DQData d) {
            sensorValue(d.getTimestamp(), new double[]{d.getValue()});
        }

        void notify(DQData d) {
            StringBuilder s = new StringBuilder(100);
            s.append("fieldName\ttoString");
            for (Field i : d.getClass().getFields()) {
                s.append(i.getName());
                s.append("\t");
                try {
                    s.append(i.get(d));
                } catch (IllegalAccessException e) {
                    s.append("error");
                    e.printStackTrace();
                }
                s.append("\n");
            }
            sensorEvent(d.getTimestamp(), EC_META, s.toString());
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object)"timestamp", "value");
        }

        @Override
        public String getName() {
            return "CarData-" + name;
        }
    }
}
