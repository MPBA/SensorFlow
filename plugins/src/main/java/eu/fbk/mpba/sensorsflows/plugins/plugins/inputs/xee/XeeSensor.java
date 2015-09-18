package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import com.dquid.xee.sdk.DQAccelerometerData;
import com.dquid.xee.sdk.DQData;
import com.dquid.xee.sdk.DQGpsData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public abstract class XeeSensor extends SensorComponent<Long, double[]> {

    protected boolean streaming = true;
    protected boolean debug = true;
    public static final int EC_CONNECTION = 0;
    public static final int EC_META = 1;

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

        void sensorValue(Long timestamp, DQAccelerometerData d) {
            if (debug)
                sensorValue(timestamp, new double[]{d.timestamp, d.x, d.y, d.z, d.nda});
            else
                sensorValue(timestamp, new double[]{d.x, d.y, d.z, d.nda});
        }

        @Override
        public List<Object> getValueDescriptor() {
            if (debug)
                return Arrays.asList((Object)"xee_timestamp", "x", "y", "z", "nda");
            else
                return Arrays.asList((Object)"x", "y", "z", "nda");
        }

    }

    public static class XeeGPS extends XeeSensor {
        protected XeeGPS(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

        void sensorValue(Long timestamp, DQGpsData d) {
            if (debug)
                sensorValue(timestamp, new double[]{ d.timestamp, d.latitude, d.longitude, d.altitude, d.heading, d.latitude_direction, d.longitude_direction, d.satellites });
            else
                sensorValue(timestamp, new double[]{ d.latitude, d.longitude, d.altitude, d.heading, d.latitude_direction, d.longitude_direction, d.satellites });

        }

        @Override
        public List<Object> getValueDescriptor() {
            if (debug)
                return Arrays.asList((Object)"xee_timestamp", "latitude", "longitude", "altitude", "heading", "latitude_direction", "longitude_direction", "satellites");
            else
                return Arrays.asList((Object)"latitude", "longitude", "altitude", "heading", "latitude_direction", "longitude_direction", "satellites");
        }

    }

    public static class CarData extends XeeSensor {
        String name;

        protected CarData(DevicePlugin<Long, double[]> parent, String name) {
            super(parent);
            this.name = name;
        }

        void sensorValue(Long timestamp, DQData d) {
            if (debug)
                sensorValue(timestamp, new double[]{d.getTimestamp(), d.getValue()});
            else
                sensorValue(timestamp, new double[]{d.getValue()});
        }

        void sendMeta(DQData d) {
            StringBuilder s = new StringBuilder(100);
            s.append("property\tvalue\n");
            for (Method i : d.getClass().getMethods())
                if (i.getName().startsWith("get")) {
                    s.append(i.getName());
                    s.append("\t");
                    try {
                        //noinspection NullArgumentToVariableArgMethod
                        s.append(i.invoke(d, (Object[]) null).toString().replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n"));
                    } catch (IllegalAccessException e) {
                        s.append("error");
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    s.append("\n");
                }
            sensorEvent(d.getTimestamp(), EC_META, s.toString());
        }

        @Override
        public List<Object> getValueDescriptor() {
            if (debug)
                return Arrays.asList((Object)"xee_timestamp", "value");
            else
                return Collections.singletonList((Object) "value");
        }

        @Override
        public String getName() {
            return "CarData-" + name;
        }
    }
}
