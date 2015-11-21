package eu.fbk.mpba.sensorsflows.plugins.inputs.comftech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

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

        public enum SubType {
            ECG_SAMP_FREQ, ECG_COMP_HR, ECG_SENSOR_STATUS, ECG_VALUE,
            MEMS_XYZ
        }

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

    // Util consts

    //
    //  1 Normal / low power mode (1 Hz)
    //  2 Normal / low power mode (10 Hz)
    //  3 Normal / low power mode (25 Hz)
    //  4 Normal / low power mode (50 Hz)
    //  5 Normal / low power mode (100 Hz)
    //  6 Normal / low power mode (200 Hz)
    //
    final static short[] memsfreqs = { 0, 1, 10, 25, 50, 100, 200 };
    final static byte memsFMin = 1;
    final static byte memsFMax = 1;

    // Operation

    long nextEcg = -1, nextMems = 0, lost = 0, ok = 0, invalidChecksum = 0;

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    protected void received(final byte[] payload, final int bytes) {
        ByteBuffer pack = ByteBuffer.wrap(payload, 0, bytes);
        switch (payload[0]) {
            case Packet.ECG_WAVEFORM: {
                // Timestamp relativo all’istante di inizio campionamento
                long timestamp_ECG_PckStart;
                // Numero sequenziale assegnato al primo campionamento del pacchetto.
                long Samplenum;
                // Frequenza di campionamento dell'onda elettrocardiografica
                int sampleFrequencyECG;
                // Frequenza cardiaca rilevata
                int HeartRate;
                // Indica che i sensori risultano sconnessi
                int SensorStatus;
                // Numero di Samples
                int n = (bytes - 13) / 2;
                // Vettore contenente l'informazione per la ricostruzione della waveform
                int[] ECGSamples = new int[n];

                // Control
                timestamp_ECG_PckStart = pack.getInt() & 0x0000_0000_FFFF_FFFFL;    // 4B U
                Samplenum = pack.getInt() & 0x0000_0000_FFFF_FFFFL;                 // 4B U
                sampleFrequencyECG = pack.getShort() & 0x00_00_FF_FF;               // 2B U

                // Data
                HeartRate = pack.getShort() & 0x00_00_FF_FF;                        // 2B U
                SensorStatus = pack.get() & 0x00_00_00_FF;                          // 1B U
                for (int i = 0; i < n; i++)
                    ECGSamples[i] = pack.getShort() & 0x00_00_FF_FF;                // 2B U

                if (nextEcg < Samplenum) {
                    // Lost: jump
                    Log.e("CozyDebug", "Lost ECG samples: " + (nextEcg - Samplenum));
                    mDataDelegate.lostSamples(this, Packet.SubType.ECG_VALUE, (int) (nextEcg - Samplenum));
                    nextEcg = Samplenum;
                }
                if (nextEcg > Samplenum) {
                    // Duplicated: discard (bug?)
                    Log.e("CozyDebug", "Duplicated ECG samples: " + (Samplenum - nextEcg));
                    mDataDelegate.duplicateSamples(this, Packet.SubType.ECG_VALUE, (int) (nextEcg - Samplenum));
                    nextEcg = Samplenum;
                }

                for (int i = 0; i < n; i++)
                    mDataDelegate.received(this, Packet.SubType.ECG_VALUE,
                            timestamp_ECG_PckStart * 1000 + i * 1000 / sampleFrequencyECG,
                            new double[] { ECGSamples[i] });

                mDataDelegate.received(this, Packet.SubType.ECG_COMP_HR,
                        timestamp_ECG_PckStart * 1000 - 1000 / sampleFrequencyECG,
                        new double[] { HeartRate });

                mDataDelegate.received(this, Packet.SubType.ECG_SENSOR_STATUS,
                        timestamp_ECG_PckStart * 1000 - 1000 / sampleFrequencyECG,
                        new double[] { SensorStatus });

                nextEcg = Samplenum + n;
            }
                break;
            case Packet.MEMS_WAVEFORM: {
                // Timestamp relativo all'istante di inizio campionamento
                long timestamp_MEMS_PckStart;
                // Numero sequenziale assegnato al primo campionamento del pacchetto.
                long Samplenum;
                // Frequenza di campionamento delle accelerazioni.
                // Per la decodifica dell’informazione si veda il comando FMEMS
                int sampleFrequencyMEMS;
                // Numer of samples
                int n = 3 * (bytes - 9) / 2;
                // Vettore contenente l'informazione per la ricostruzione della waveform.
                // Ogni campione è costituito da una tripletta di valori relativi,
                // rispettivamente, alle accelerazioni X, Y e Z.
                int[] MEMSSamples = new int[n];

                timestamp_MEMS_PckStart = pack.getInt() & 0x0000_0000_FFFF_FFFFL;   // 4B U
                Samplenum = pack.getInt() & 0x0000_0000_FFFF_FFFFL;                 // 4B U
                sampleFrequencyMEMS = pack.get() & 0x00_00_00_FF;                   // 1B U
                if (sampleFrequencyMEMS >= memsFMin && sampleFrequencyMEMS <= memsFMax)
                    sampleFrequencyMEMS = memsfreqs[sampleFrequencyMEMS];
                else
                    // Pack everything to avoid overlapping
                    sampleFrequencyMEMS = memsfreqs[memsFMax];
                for (int i = 0; i < n; i++)
                    MEMSSamples[i] = pack.getShort() & 0x00_00_FF_FF;               // 2B U

                if (nextMems < Samplenum) {
                    // Lost: jump
                    Log.e("CozyDebug", "Lost MEMS samples: " + (nextMems - Samplenum));
                    mDataDelegate.lostSamples(this, Packet.SubType.ECG_VALUE, (int) (nextMems - Samplenum));
                    nextMems = Samplenum - 1;
                }
                if (nextMems > Samplenum) {
                    // Duplicated: discard (bug?)
                    Log.e("CozyDebug", "Duplicated MEMS samples: " + (Samplenum - nextMems));
                    mDataDelegate.duplicateSamples(this, Packet.SubType.ECG_VALUE, (int) (nextMems - Samplenum));
                    nextMems = Samplenum;
                }

                for (int i = 0; i < n;)
                    mDataDelegate.received(this, Packet.SubType.MEMS_XYZ,
                            timestamp_MEMS_PckStart * 1000 + 1000 / sampleFrequencyMEMS,
                            new double[] { MEMSSamples[i++], MEMSSamples[i++], MEMSSamples[i++] });

                nextMems = Samplenum + n;
            }
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

        void received(CozyBabyManager sender, Packet.SubType type, Long timestamp, double[] value);

        void lostSamples(CozyBabyManager sender, Packet.SubType type, int howMany);

        void duplicateSamples(CozyBabyManager sender, Packet.SubType type, int howMany);

        void lostPacket(CozyBabyManager sender, int type);
    }
}
