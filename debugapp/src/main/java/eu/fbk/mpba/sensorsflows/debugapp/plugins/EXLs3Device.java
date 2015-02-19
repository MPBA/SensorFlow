package eu.fbk.mpba.sensorsflows.debugapp.plugins;

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

    public EXLs3Device(BluetoothDevice realDevice, String name) {
        this.name = name;
        resetMonoTimestamp(System.currentTimeMillis(), System.nanoTime());
        _sensor = new EXLSensor(this, realDevice);
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
    
    private static class EXLSensor extends SensorComponent<Long, double[]> {

        EXLs3Manager manager;
        DevicePlugin<Long, double[]> parent;
        String name;
        String address;
        BluetoothDevice dev;
        
        protected EXLSensor(DevicePlugin<Long, double[]> parent, BluetoothDevice device) {
            super(parent);
            this.parent = parent;
            manager = new EXLs3Manager(btsStatus, btsData);
            name = device.getName();
            address = device.getAddress();
            dev = device;
        }

        public void connect() {
            manager.connect(dev, false);
        }

        @Override
        public void switchOnAsync() {
            manager.sendStart();
        }

        @Override
        public void switchOffAsync() {
            manager.sendStop();
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object)"counter", "ax", "ay", "az", "gx", "gy", "gz", "mx", "my", "mz", "q1", "q2", "q3", "q4", "vbatt");
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

            public void listening(EXLs3Manager sender) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "listening");
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

            public void connectionFailed(EXLs3Manager sender) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "connectionFailed");
            }

            public void connectionLost(EXLs3Manager sender) {
                sensorEvent(((IMonotonicTimestampReference)parent).getMonoTimestampNanos(System.nanoTime()),
                        1, "connectionLost");
            }
        };

        private final long freq = 100;
        private final long mpkc = 10000_000000L;
        
        private final EXLs3Manager.DataDelegate btsData = new EXLs3Manager.DataDelegate() {
            long st = -1;
            long lrc = -1;
            long lpkc = -1;

            @Override
            public void receive(EXLs3Manager sender, EXLs3Manager.Packet p) {
                lrc = ((IMonotonicTimestampReference)parent).getMonoTimestampNanos(p.receprionNanos);
                if (st < 0)
                    st = lrc - p.counter / freq;

                long ft = p.counter / freq + st;

                if (Math.abs(ft - lrc) > (0.05 * mpkc / freq)) {
                    sensorEvent(lrc, 3, "Warning, packet time drift over 10% of packet counter cycle time, resetting reference.");
                    Log.e("@@@", "resetting reference " + (Math.abs(ft - lrc) - (0.05 * mpkc / freq)));
                    st = lrc - p.counter / freq;
                }

                sensorValue(ft, new double[] { lpkc = p.counter, p.ax, p.ay, p.az, p.gx, p.gy, p.gz, p.mx, p.my, p.mz, p.q1, p.q2, p.q3, p.q4, p.vbatt } );
            }
        };
    }
}
