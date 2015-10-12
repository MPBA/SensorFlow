package eu.fbk.mpba.sensorsflows.plugins.inputs.comftech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.nio.ByteBuffer;

public class CozyBabyManager extends CozyBabyReceiver {

    // TODO 0: implement protocol
    // - LSB first, Little Endian

    // Debug
    private static final String TAG = CozyBabyManager.class.getSimpleName();

    // Constants
    public static final int MAX_WRONG_PACKETS = 10;

    protected static class Packet {
        public static final int ECG_WAVEFORM = 0x01;
        public static final int FIRMWARE_VERSION = 0x02;
        public static final int BATTERY_INFO = 0x03;
        public static final int ORIENTATION = 0x04;
        public static final int MEMS_WAVEFORM = 0x05;
        public static final int BEAT_ANNOTATION = 0x06;
        public static final int HEART_BPM = 0x0d;
        public static final int SYSTEM_INFO = 0x0e;
        public static final int DATA_LOSS = 0x0f;

        public final int first;

        Packet(int h) {
            first = h;
        }
    }

    // Member fields
    private final DataDelegate mDataDelegate;

    public CozyBabyManager(StatusDelegate statusDelegate, DataDelegate dataDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        super(statusDelegate, device, adapter);
        mDataDelegate = dataDelegate;
    }

    // Operation

    int last = -1, wrong = 0, lost = 0, ok = 0, invalidChecksum = 0;

    @Override
    protected void received(final byte[] payload, final int bytes) {
        switch (payload[0]) {
            case Packet.ECG_WAVEFORM:
                ByteBuffer pack = ByteBuffer.wrap(payload);
                int ts, samp, freq, status;

                break;
            case Packet.MEMS_WAVEFORM:
                break;
            case Packet.HEART_BPM:
                break;
            case Packet.ORIENTATION:
                break;
            case Packet.BEAT_ANNOTATION:
                break;
            case Packet.DATA_LOSS:
                break;
            case Packet.BATTERY_INFO:
                break;
            case Packet.FIRMWARE_VERSION:
                break;
            case Packet.SYSTEM_INFO:
                break;
        }
    }

    public void connect() {
        super.connect();
    }

    public void stop() {
        close();
    }

    // Subclasses

    public interface DataDelegate {
        void received(CozyBabyManager sender, Packet p);

        void lost(CozyBabyManager sender, int from, int to, int howMany);
    }
}
