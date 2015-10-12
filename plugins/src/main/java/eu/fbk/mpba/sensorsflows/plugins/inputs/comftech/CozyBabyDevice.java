package eu.fbk.mpba.sensorsflows.plugins.inputs.comftech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class CozyBabyDevice implements DevicePlugin<Long, double[]> {

    // TODO 4: replayable

    private String name;
    private EXLSensor monoSensor;

    EXLAccelerometer er;
    EXLBattery ry;
    EXLGyroscope pe;
    EXLMagnetometer ma;
    EXLQuaternion on;

    public CozyBabyDevice(BluetoothDevice realDevice, BluetoothAdapter adapter, String name) {
        this.name = name;
        monoSensor = new EXLSensor(this, realDevice, adapter);
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(Arrays.asList((SensorComponent<Long, double[]>) er, pe, ma, on, ry).iterator());
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void close() {
        monoSensor.disconnect();
    }

    private boolean connected = false;

    public void connect() {
        if (!connected)
            monoSensor.connect();
    }

    public CozyBabyReceiver.BTSrvState getConnectionState() {
        return monoSensor.manager.getState();
    }

    @Override
    public void inputPluginInitialize() {
        if (!connected)
            monoSensor.connect();
        monoSensor.switchDevOnAsync();
    }

    @Override
    public void inputPluginFinalize() {
        monoSensor.disconnect();
    }

    @Override
    public String getName() {
        return name;
    }

    public static class EXLSensor extends SensorComponent<Long, double[]> {

        boolean streaming = true;
        int received = 0;

        CozyBabyManager manager;
        CozyBabyDevice parent;
        String name;
        String address;
        BluetoothDevice dev;
        
        protected EXLSensor(CozyBabyDevice parent, BluetoothDevice device, BluetoothAdapter adapter) {
            this(parent);
            dev = device;
            address = device.getAddress();
            manager = new CozyBabyManager(btsStatus, btsData, device, adapter);
        }

        protected EXLSensor(CozyBabyDevice parent) {
            super(parent);
            this.parent = parent;
            name = parent.name;
        }

        @Override
        public List<Object> getValueDescriptor() {
            throw new UnsupportedOperationException("Do not use this sensor.");
        }

        public void connect() {
            manager.connect();
        }

        public void disconnect() {
            manager.stop();
        }

        public void switchDevOnAsync() {
            manager.command(CozyBabyReceiver.Commands.startStreaming);
        }

        public void switchDevOffAsync() {
            manager.command(CozyBabyReceiver.Commands.stopStreaming);
        }

        @Override
        public void switchOnAsync() {
            streaming = true;
        }

        @Override
        public void switchOffAsync() {
            streaming = false;
        }

        private final CozyBabyManager.StatusDelegate btsStatus = new CozyBabyManager.StatusDelegate() {

            public void idle(CozyBabyReceiver sender) {
                sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        READY, "idle");
            }

            public void connecting(CozyBabyReceiver sender, BluetoothDevice device, boolean secureMode) {
                parent.er.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        CONNECTING, "connecting to " + device.getName() + "@" + device.getAddress() + (secureMode ? " secure" : " insecure") + " mode");
            }

            public void connected(CozyBabyReceiver sender, String deviceName) {
                parent.er.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        CONNECTED, "connected to " + deviceName);
            }

            public void disconnected(CozyBabyReceiver sender, DisconnectionCause cause) {
                parent.er.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        DISCONNECTED | cause.flag, "disconnected:" + cause.toString());
            }
        };

        private final long freq = 100;
        private final long max = 10000;
        private final long cycle = max * 1_000000L / freq;
        
        private final CozyBabyManager.DataDelegate btsData = new CozyBabyManager.DataDelegate() {
            long ref = -1;
            long now = -1;
            long pre = 0;
            int last = -1; // val ok
            int qd = 0, bd = 0;

            @Override
            public void received(CozyBabyManager sender, CozyBabyManager.Packet p) {
//                // TODO! check timestamp calc
//                now = getTime().getMonoUTCNanos(p.receptionTime);
//                if (ref < 0) {
//                    pre = ref = now - p.counter * 1000_000000L / freq; // pk0 cTime = now - time from pk0 to pkThis
//                }
//
//                long calc = pre += (p.lostFromPreviousCounter(last) + 1) * 1000_000000L / freq;
//
//                received++;
//
//                if (parent.er.streaming)
//                    parent.er.sensorValue(now, new double[]{p.ax, p.ay, p.az});
//                if (parent.pe.streaming)
//                    parent.pe.sensorValue(now, new double[]{p.gx, p.gy, p.gz});
//                if (parent.ma.streaming)
//                    parent.ma.sensorValue(now, new double[]{p.mx, p.my, p.mz});
//                if (parent.on.streaming && (qd++ % parent.qDS) == 0)
//                    parent.on.sensorValue(now, new double[]{p.q1, p.q2, p.q3, p.q4});
//                if (parent.ry.streaming && (bd++ % parent.bDS) == 0)
//                    parent.ry.sensorValue(now, new double[]{p.vbatt});
//
//                last = p.counter;
            }

            @Override
            public void lost(CozyBabyManager sender, int from, int to, int howMany) {
                Log.v(this.getClass().getSimpleName(), "lost:" + howMany + " fr:" + from + " to:" + to);
            }
        };

        @Override
        public int getReceivedMessagesCount() {
            return received;
        }
    }

    public static class EXLAccelerometer extends CozyBabyDevice.EXLSensor {

        protected EXLAccelerometer(CozyBabyDevice parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "ax", "ay", "az");
        }

    }

    public static class EXLGyroscope extends CozyBabyDevice.EXLSensor {

        protected EXLGyroscope(CozyBabyDevice parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "gx", "gy", "gz");
        }

    }

    public static class EXLMagnetometer extends CozyBabyDevice.EXLSensor {

        protected EXLMagnetometer(CozyBabyDevice parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "mx", "my", "mz");
        }

    }

    public static class EXLQuaternion extends CozyBabyDevice.EXLSensor {

        protected EXLQuaternion(CozyBabyDevice parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "q1", "q2", "q3", "q4");
        }

    }

    public static class EXLBattery extends CozyBabyDevice.EXLSensor {

        protected EXLBattery(CozyBabyDevice parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "vbatt");
        }

    }
}
