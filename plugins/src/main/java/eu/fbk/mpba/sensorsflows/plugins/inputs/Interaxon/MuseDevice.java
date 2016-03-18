package eu.fbk.mpba.sensorsflows.plugins.inputs.Interaxon;

import android.util.Log;

import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.Battery;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class MuseDevice implements DevicePlugin<Long, double[]> {

    private Muse mMuse = null;
    private String name;

    ConnectionListener connectionListener = this.new ConnectionListener();
    DataListener dataListener = this.new DataListener();

    EEG eeg = this.new EEG();
    ACCELEROMETER accelerometer = this.new ACCELEROMETER();
    ALPHA_RELATIVE alphaRelative = this.new ALPHA_RELATIVE();
    BATTERY battery = this.new BATTERY();

    public MuseDevice(String name) {
        this.name = name;
    }

    public void connect(Muse muse) {
        if (mMuse != null)
            mMuse.disconnect(true);
        mMuse = muse;
        ConnectionState state = mMuse.getConnectionState();
        if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
            Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
        } else {

            mMuse.registerConnectionListener(connectionListener);
            mMuse.registerDataListener(dataListener,
                    MuseDataPacketType.ACCELEROMETER);
            mMuse.registerDataListener(dataListener,
                    MuseDataPacketType.EEG);
            mMuse.registerDataListener(dataListener,
                    MuseDataPacketType.ALPHA_RELATIVE);
            mMuse.registerDataListener(dataListener,
                    MuseDataPacketType.ARTIFACTS);
            mMuse.registerDataListener(dataListener,
                    MuseDataPacketType.BATTERY);
            mMuse.setPreset(MusePreset.PRESET_14);
            mMuse.enableDataTransmission(false);

            try {
                mMuse.runAsynchronously();
            } catch (Exception e) {
                Log.e("Muse Headband", e.toString());
            }
        }
    }

    public void disconnect() {
        mMuse.disconnect(true);
    }

    @Override
    public void inputPluginInitialize() {
        mMuse.enableDataTransmission(true);
    }

    @Override
    public void inputPluginFinalize() {
        mMuse.enableDataTransmission(false);
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        disconnect();
    }

    private void broadcastEvent(int code, String message) {
        Long time = eeg.getTime().getMonoUTCNanos();
        eeg.sensorEvent(time, code, message);
        accelerometer.sensorEvent(time, code, message);
        alphaRelative.sensorEvent(time, code, message);
        battery.sensorEvent(time, code, message);
    }

    class ConnectionListener extends MuseConnectionListener {

        // TODO: events

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() +
                    " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() +
                    " " + status;

            Log.i("Muse Headband", full);

            // statusText status

            if (current == ConnectionState.CONNECTED) {
                MuseVersion museVersion = mMuse.getMuseVersion();
                String version = museVersion.getFirmwareType() +
                        " - " + museVersion.getFirmwareVersion() +
                        " - " + Integer.toString(
                        museVersion.getProtocolVersion());
                // museVersionText version
            } else {
                // museVersionText undefined
            }
        }
    }

    int
            EV_ARTIFACT = 10;

    class DataListener extends MuseDataListener {

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            ArrayList<Double> data = p.getValues();
            // TODO: improve as .ordinal() is unuseful
            switch (p.getPacketType()) {
                case EEG:
                    eeg.sensorValue(eeg.getTime().getMonoUTCNanos(), new double[]{
                            data.get(Eeg.TP9.ordinal()),
                            data.get(Eeg.FP1.ordinal()),
                            data.get(Eeg.FP2.ordinal()),
                            data.get(Eeg.TP10.ordinal())
                    });
                    break;
                case ACCELEROMETER:
                    accelerometer.sensorValue(accelerometer.getTime().getMonoUTCNanos(), new double[]{
                            data.get(Accelerometer.FORWARD_BACKWARD.ordinal()),
                            data.get(Accelerometer.UP_DOWN.ordinal()),
                            data.get(Accelerometer.LEFT_RIGHT.ordinal())
                    });
                    break;
                case ALPHA_RELATIVE:
                    alphaRelative.sensorValue(alphaRelative.getTime().getMonoUTCNanos(), new double[]{
                            data.get(Eeg.TP9.ordinal()),
                            data.get(Eeg.FP1.ordinal()),
                            data.get(Eeg.FP2.ordinal()),
                            data.get(Eeg.TP10.ordinal())
                    });
                    break;
                case BATTERY:
                    battery.sensorValue(battery.getTime().getMonoUTCNanos(), new double[]{
                            data.get(Battery.CHARGE_PERCENTAGE_REMAINING.ordinal()),
                            data.get(Battery.MILLIVOLTS.ordinal()),
                            data.get(Battery.TEMPERATURE_CELSIUS.ordinal())
                    });
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn()) {
                if (p.getBlink()) {
                    broadcastEvent(EV_ARTIFACT, "blink");
                }
                if (p.getJawClench()) {
                    broadcastEvent(EV_ARTIFACT, "jaw_clanch");
                }
            }
        }
    }

    public abstract class MuseSensor extends SensorComponent<Long, double[]> {

        protected MuseSensor() {
            super(MuseDevice.this);
        }

        @Override
        public void switchOnAsync() {

        }

        @Override
        public void switchOffAsync() {

        }
    }

    public class EEG extends MuseSensor {

        @Override
        public List<Object> getValueDescriptor() {
            return null;
        }
    }

    public class ACCELEROMETER extends MuseSensor {

        @Override
        public List<Object> getValueDescriptor() {
            return null;
        }
    }

    public class ALPHA_RELATIVE extends MuseSensor {

        @Override
        public List<Object> getValueDescriptor() {
            return null;
        }
    }

    public class BATTERY extends MuseSensor {

        @Override
        public List<Object> getValueDescriptor() {
            return null;
        }
    }
}
