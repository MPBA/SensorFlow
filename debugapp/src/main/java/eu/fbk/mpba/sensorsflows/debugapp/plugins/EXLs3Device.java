package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.debugapp.util.EXLs3Manager;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class EXLs3Device implements DevicePlugin<Long, double[]>, IMonotonicTimestampReference {

    private String name;
    private EXLSensor _sensor;

    EXLAccelerometer er;
    EXLBattery ry;
    EXLGyroscope pe;
    EXLMagnetometer ma;
    EXLQuaternion on;

    public EXLs3Device(BluetoothDevice realDevice, BluetoothAdapter adapter, String name) {
        this.name = name;
        resetMonoTimestamp(System.currentTimeMillis(), System.nanoTime());
        er = new EXLAccelerometer(this);
        ry = new EXLBattery(this);
        pe = new EXLGyroscope(this);
        ma = new EXLMagnetometer(this);
        on = new EXLQuaternion(this);
        _sensor = new EXLSensor(this, realDevice, adapter);
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(Arrays.asList((SensorComponent<Long, double[]>) _sensor).iterator());
    }

    @Override
    public void inputPluginInitialize() {
        _sensor.connect();
        _sensor.switchOnAsync();
    }

    @Override
    public void inputPluginFinalize() {
        _sensor.switchOffAsync();
    }

    private long bootUTCNanos;

    public void resetMonoTimestamp(long timestamp, long realTimeNanos) {
        bootUTCNanos = timestamp * 1000000 - realTimeNanos;
    }

    public long getMonoTimestampNanos(long realTimeNanos) {
        return realTimeNanos + bootUTCNanos;
    }

    @Override
    public String toString() {
        return name;
    }

    public static class EXLSensor extends SensorComponent<Long, double[]> {

        EXLs3Manager manager;
        EXLs3Device parent;
        String name;
        String address;
        BluetoothDevice dev;
        
        protected EXLSensor(EXLs3Device parent, BluetoothDevice device, BluetoothAdapter adapter) {
            super(parent);
            this.parent = parent;
            manager = new EXLs3Manager(btsStatus, btsData, device, adapter);
            name = device.getName();
            address = device.getAddress();
            dev = device;
        }

        protected EXLSensor(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

        public void connect() {
            manager.connect();
        }

        @Override
        public void switchOnAsync() {
            manager.startStream();
        }

        @Override
        public void switchOffAsync() {
            manager.stopStream();
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return manager.getValuesDescriptors();
        }

        @Override
        public String toString() {
            return address.replace(":", "") + "_" + name;
        }

        private final EXLs3Manager.StatusDelegate btsStatus = new EXLs3Manager.StatusDelegate() {

            public void idle(EXLs3Manager sender) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "idle");
            }

            public void wrongPacketType(EXLs3Manager sender) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "wrongPacketType");
            }

            public void connecting(EXLs3Manager sender, BluetoothDevice device, boolean secureMode) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "connecting");
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        0, "connecting to " + device.getName() + "@" + device.getAddress() + (secureMode ? " secure" : " insecure") + " mode");
            }

            public void connected(EXLs3Manager sender, String deviceName) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "connected");
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        0, "connected to " + deviceName);
            }

            public void disconnected(EXLs3Manager sender, Cause cause) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "disconnected:" + cause.toString());
            }
        };

        private final long freq = 100;
        private final long mpc = 10000_000000L;
        
        private final EXLs3Manager.DataDelegate btsData = new EXLs3Manager.DataDelegate() {
            long st = -1;
            long lrc = -1;
            long lpc = -1;

            @Override
            public void receive(EXLs3Manager sender, EXLs3Manager.Packet p) {
                lrc = ((IMonotonicTimestampReference)parent).getMonoTimestampNanos(p.receptionTime);
                if (st < 0)
                    st = lrc - p.counter * 1000_000000L / freq;

                long ft = p.counter / freq * 1000_000000L + st;

                if (Math.abs(ft - lrc) > (mpc / 10)) {
                    sensorEvent(lrc, 3, "TimeDrift > 10%, ref reset");
                    Log.e("@@@", "resetting reference " + ((ft - lrc) + " > " + (mpc / 10)));
                    st = -25;
                }
                lpc = p.counter;
                parent.er.sensorValue(ft, new double[] { p.ax, p.ay, p.az } );
                parent.pe.sensorValue(ft, new double[] { p.gx, p.gy, p.gz } );
                parent.ma.sensorValue(ft, new double[] { p.mx, p.my, p.mz } );
                parent.on.sensorValue(ft, new double[] { p.q1, p.q2, p.q3, p.q4 } );
                parent.ry.sensorValue(ft, new double[] { p.vbatt } );
            }
        };
    }

    public class EXLAccelerometer extends EXLs3Device.EXLSensor {

        protected EXLAccelerometer(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

    }

    public class EXLGyroscope extends EXLs3Device.EXLSensor {

        protected EXLGyroscope(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

    }

    public class EXLMagnetometer extends EXLs3Device.EXLSensor {

        protected EXLMagnetometer(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

    }

    public class EXLQuaternion extends EXLs3Device.EXLSensor {

        protected EXLQuaternion(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

    }

    public class EXLBattery extends EXLs3Device.EXLSensor {

        protected EXLBattery(DevicePlugin<Long, double[]> parent) {
            super(parent);
        }

    }
}
