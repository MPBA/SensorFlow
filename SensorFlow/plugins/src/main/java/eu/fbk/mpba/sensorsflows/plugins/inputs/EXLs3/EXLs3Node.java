package eu.fbk.mpba.sensorsflows.plugins.inputs.EXLs3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class EXLs3Node implements NodePlugin<Long, double[]> {

    // TODO 4: replayable

    private String name;
    private EXLSensor monoSensor;

    EXLSamplenum sn;
    EXLAccelerometer er;
    EXLBattery ry;
    EXLGyroscope pe;
    EXLMagnetometer ma;
    EXLQuaternion on;

    protected final int qDS, bDS;

    public EXLs3Node(BluetoothDevice realDevice, BluetoothAdapter adapter, String name, int quaternionDecimation, int batteryDecimation) {
        this.name = name;
        sn = new EXLSamplenum(this);
        sn.switchOffAsync();
        er = new EXLAccelerometer(this);
        ry = new EXLBattery(this);
        pe = new EXLGyroscope(this);
        ma = new EXLMagnetometer(this);
        on = new EXLQuaternion(this);
        monoSensor = new EXLSensor(this, realDevice, adapter);
        qDS = quaternionDecimation;
        bDS = batteryDecimation;
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

    public void connect(EXLs3Receiver.ConnectionCallback c) {
        monoSensor.connect(c);
    }

    @Override
    public void inputPluginStart() {
        monoSensor.switchDevOnAsync();
    }

    @Override
    public void inputPluginStop() {
        monoSensor.disconnect();
    }

    @Override
    public String getName() {
        return name;
    }

    public static class EXLSensor extends SensorComponent<Long, double[]> {

        boolean streaming = true;
        int received = 0;

        EXLs3Manager manager;
        EXLs3Node parent;
        String name;
        String address;
        BluetoothDevice dev;
        
        protected EXLSensor(EXLs3Node parent, BluetoothDevice device, BluetoothAdapter adapter) {
            this(parent);
            dev = device;
            address = device.getAddress();
            manager = new EXLs3Manager(btsStatus, btsData, device, adapter);
        }

        protected EXLSensor(EXLs3Node parent) {
            super(parent);
            this.parent = parent;
            name = parent.name;
        }

        @Override
        public List<Object> getValueDescriptor() {
            throw new UnsupportedOperationException("Do not use this sensor.");
        }

        public void connect(EXLs3Receiver.ConnectionCallback c) {
            manager.connect(c);
        }

        public void disconnect() {
            manager.stop();
        }

        public void switchDevOnAsync() {
            manager.startStream();
        }

        public void switchDevOffAsync() {
            manager.stopStream();
        }

        @Override
        public void switchOnAsync() {
            streaming = true;
        }

        @Override
        public void switchOffAsync() {
            streaming = false;
        }

        private final EXLs3Manager.StatusDelegate btsStatus = new EXLs3Manager.StatusDelegate() {

            public void idle(EXLs3Receiver sender) {
                sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        READY, "idle");
            }

            public void connecting(EXLs3Receiver sender, BluetoothDevice device, boolean secureMode) {
                parent.er.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        CONNECTING, "connecting to " + device.getName() + "@" + device.getAddress() + (secureMode ? " secure" : " insecure") + " mode");
            }

            public void connected(EXLs3Receiver sender, String deviceName) {
                parent.er.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        CONNECTED, "connected to " + deviceName);
            }

            public void disconnected(EXLs3Receiver sender, DisconnectionCause cause) {
                parent.er.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        DISCONNECTED | cause.flag, "disconnected:" + cause.toString());
            }
        };
        
        private final EXLs3Manager.DataDelegate btsData = new EXLs3Manager.DataDelegate() {
            long now = -1;
            int last = -1; // val ok
            int qd = 0, bd = 0;

            @Override
            public void received(EXLs3Manager sender, EXLs3Manager.Packet p) {
                // TODO! check timestamp calc
                now = getTime().getMonoUTCNanos(p.receptionTime);

                received++;

                if (parent.sn.streaming)
                    parent.sn.sensorValue(now, new double[]{p.counter});
                if (parent.er.streaming)
                    parent.er.sensorValue(now, new double[]{p.ax, p.ay, p.az});
                if (parent.pe.streaming)
                    parent.pe.sensorValue(now, new double[]{p.gx, p.gy, p.gz});
                if (parent.ma.streaming)
                    parent.ma.sensorValue(now, new double[]{p.mx, p.my, p.mz});
                if (parent.on.streaming && (qd++ % parent.qDS) == 0)
                    parent.on.sensorValue(now, new double[]{p.q1, p.q2, p.q3, p.q4});
                if (parent.ry.streaming && (bd++ % parent.bDS) == 0)
                    parent.ry.sensorValue(now, new double[]{p.vbatt});

                last = p.counter;
            }

            @Override
            public void lost(EXLs3Manager sender, int from, int to, int howMany) {
                Log.v(EXLs3Node.class.getSimpleName() + " " + sender.mDevice.getAddress(), "lost:" + howMany + " fr:" + from + " to:" + to);
            }
        };
    }

    public static class EXLSamplenum extends EXLs3Node.EXLSensor {

        protected EXLSamplenum(EXLs3Node parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "number");
        }

    }

    public static class EXLAccelerometer extends EXLs3Node.EXLSensor {

        protected EXLAccelerometer(EXLs3Node parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "ax", "ay", "az");
        }

    }

    public static class EXLGyroscope extends EXLs3Node.EXLSensor {

        protected EXLGyroscope(EXLs3Node parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "gx", "gy", "gz");
        }

    }

    public static class EXLMagnetometer extends EXLs3Node.EXLSensor {

        protected EXLMagnetometer(EXLs3Node parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "mx", "my", "mz");
        }

    }

    public static class EXLQuaternion extends EXLs3Node.EXLSensor {

        protected EXLQuaternion(EXLs3Node parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "q1", "q2", "q3", "q4");
        }

    }

    public static class EXLBattery extends EXLs3Node.EXLSensor {

        protected EXLBattery(EXLs3Node parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "vbatt");
        }

    }
}
