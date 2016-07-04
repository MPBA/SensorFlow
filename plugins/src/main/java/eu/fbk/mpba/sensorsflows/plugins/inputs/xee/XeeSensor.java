package eu.fbk.mpba.sensorsflows.plugins.inputs.xee;

import android.os.SystemClock;

import com.dquid.xee.sdk.DQAccelerometerData;
import com.dquid.xee.sdk.DQData;
import com.dquid.xee.sdk.DQGpsData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public abstract class XeeSensor extends SensorComponent<Long, double[]> {

    protected boolean streaming = true;
    protected boolean debug = true;
    public static final int EC_CONNECTION = 0;
    public static final int EC_META = 1;
    public static final int EC_OFFSET = 2;

    protected int n = 0;
    protected float avgdiff = 0f;

    protected XeeSensor(NodePlugin<Long, double[]> parent) {
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

    long lastDiffSave = 0;

    protected void updateAvgDiff(Long timestamp, long timestamp2) {
        avgdiff = (avgdiff * n + (timestamp - timestamp2 * 1_000_000)) / ++n;
        // Every ten seconds or every thousand samples
        if (SystemClock.elapsedRealtime() - lastDiffSave > 10_000 || n % 1000 == 1) {
            saveAvgSmartphoneDiff();
            lastDiffSave = SystemClock.elapsedRealtime();
        }
    }

    public void saveAvgSmartphoneDiff() {
        sensorEvent(getTime().getMonoUTCNanos(), EC_OFFSET, String.valueOf(avgdiff));
    }

    public static class XeeAccelerometer extends XeeSensor {
        protected XeeAccelerometer(NodePlugin<Long, double[]> parent) {
            super(parent);
        }

        void sensorValue(Long timestamp, DQAccelerometerData d) {
            if (debug)
                sensorValue(timestamp, new double[]{d.timestamp, d.x, d.y, d.z, d.nda});
            else {
                updateAvgDiff(timestamp, d.timestamp);
                sensorValue(d.timestamp * 1_000_000, new double[]{d.x, d.y, d.z, d.nda});
            }
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
        protected XeeGPS(NodePlugin<Long, double[]> parent) {
            super(parent);
        }

        void sensorValue(Long timestamp, DQGpsData d) {
            // TO.DO 5 ask and simplify: .did not answer
            if (d.latitude_direction == 'S' && d.latitude > 0)
                d.latitude *= -1;
            if (d.longitude_direction == 'W' && d.longitude > 0)
                d.longitude *= -1;
            if (debug)
                sensorValue(timestamp, new double[]{ d.timestamp, d.latitude, d.longitude, d.altitude, d.heading, d.satellites });
            else {
                updateAvgDiff(timestamp, d.timestamp);
                sensorValue(d.timestamp * 1_000_000, new double[]{d.latitude, d.longitude, d.altitude, d.heading, d.satellites});
            }
        }

        @Override
        public List<Object> getValueDescriptor() {
            if (debug)
                return Arrays.asList((Object)"xee_timestamp", "latitude", "longitude", "altitude", "heading", "satellites");
            else
                return Arrays.asList((Object)"latitude", "longitude", "altitude", "heading", "satellites");
        }

    }

    public static class CarData extends XeeSensor {
        String name;

        protected CarData(NodePlugin<Long, double[]> parent, String name) {
            super(parent);
            this.name = name;
        }

        void sensorValue(Long timestamp, DQData d) {
            if (debug)
                sensorValue(timestamp, new double[]{d.getTimestamp(), d.getValue()});
            else {
                updateAvgDiff(timestamp, d.getTimestamp());
                sensorValue(d.getTimestamp() * 1_000_000, new double[]{d.getValue()});
            }
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
