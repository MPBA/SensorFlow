package eu.fbk.mpba.sensorsflows.plugins.inputs.comftech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class CozyBabyNode implements NodePlugin<Long, double[]> {

    // TODO 4: replayable

    private String name;
    private CBSensor monoSensor;

    CBMEMS mems;
    CBECG ecg;

    public CozyBabyNode(BluetoothDevice realDevice, BluetoothAdapter adapter, String name) {
        this.name = name;
        monoSensor = new CBSensor(this, realDevice, adapter);
        mems = new CBMEMS(this);
        ecg = new CBECG(this);
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(Arrays.asList((SensorComponent<Long, double[]>) mems, ecg).iterator());
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

    public void connect() {
        monoSensor.connect();
    }

    public CozyBabyReceiver.BTSrvState getConnectionState() {
        return monoSensor.manager.getState();
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

    public static class CBSensor extends SensorComponent<Long, double[]> {

        boolean streaming = true;
        int received = 0;

        CozyBabyManager manager;
        CozyBabyNode parent;
        String name;
        String address;
        BluetoothDevice dev;
        
        protected CBSensor(CozyBabyNode parent, BluetoothDevice device, BluetoothAdapter adapter) {
            this(parent);
            dev = device;
            address = device.getAddress();
            manager = new CozyBabyManager(btsStatus, btsData, device, adapter);
        }

        protected CBSensor(CozyBabyNode parent) {
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
        }

        public void switchDevOffAsync() {
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
                parent.ecg.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        CONNECTING, "connecting to " + device.getName() + "@" + device.getAddress() + (secureMode ? " secure" : " insecure") + " mode");
            }

            public void connected(CozyBabyReceiver sender, String deviceName) {
                parent.ecg.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        CONNECTED, "connected to " + deviceName);
            }

            public void disconnected(CozyBabyReceiver sender, DisconnectionCause cause) {
                parent.ecg.sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        (DISCONNECTED << 8) + cause.ordinal(), "disconnected:" + cause.toString());
            }
        };
        
        private final CozyBabyManager.DataDelegate btsData = new CozyBabyManager.DataDelegate() {
            @Override
            public void received(CozyBabyManager sender, CozyBabyManager.Packet.SubType type, Long timestamp, double[] value) {
                switch (type) {
                    case ECG_SAMP_FREQ:
                        break;
                    case ECG_COMP_HR:
                        break;
                    case ECG_SENSOR_STATUS:
                        break;
                    case ECG_VALUE:
                        parent.ecg.sensorValue(timestamp, value);
                        break;
                    case MEMS_XYZ:
                        parent.mems.sensorValue(timestamp, value);
                        break;
                }
            }

            @Override
            public void lostSamples(CozyBabyManager sender, CozyBabyManager.Packet.SubType type, int howMany) {
                Log.v(this.getClass().getSimpleName(), "lost samples, samples:" + howMany + " type:" + type);
            }

            @Override
            public void duplicateSamples(CozyBabyManager sender, CozyBabyManager.Packet.SubType type, int howMany) {
                Log.v(this.getClass().getSimpleName(), "duplicated samples, samples:" + howMany + " type:" + type);
            }

            @Override
            public void log(CozyBabyManager sender, String message) {
                Log.v(this.getClass().getSimpleName(), "log::" + message);
            }
        };
    }

    public static class CBECG extends CBSensor {

        protected CBECG(CozyBabyNode parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "value");
        }

    }

    public static class CBMEMS extends CBSensor {

        protected CBMEMS(CozyBabyNode parent) {
            super(parent);
        }

        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "ax", "ay", "az");
        }

    }
}
