package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.EXLs3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class EXLs3Manager extends EXLs3Receiver {

    // Debug
    private static final String TAG = EXLs3Manager.class.getSimpleName();

    // Constants
    public static final int MAX_WRONG_PACKETS = 10;

    // Member fields
    private final DataDelegate mDataDelegate;

    public EXLs3Manager(StatusDelegate statusDelegate, DataDelegate dataDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        super(statusDelegate, device, adapter);
        mDataDelegate = dataDelegate;
    }

    // Operation

    int last = -1, wrong = 0, lost = 0, ok = 0, invalidChecksum = 0;

    @Override
    protected void received(final byte[] buffer, final int bytes) {
        // Keep wrongPacketType to the InputStream while connected
        Packet p;
        p = Packet.parsePacket(System.nanoTime(), buffer);
        if (p == null) {
            if (wrong++ > MAX_WRONG_PACKETS && ok < 9 * MAX_WRONG_PACKETS) {
                Log.i(TAG, "++ Wrong packet type");
                // Closing connection and everything else
                setState(BTSrvState.DISCONNECTED);
                if (mStatusDelegate != null)
                    mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.WRONG_PACKET_TYPE);
                close();
            }
        } else {
            int nowLost = p.lostFromPreviousCounter(last);
            if (nowLost != 0) {
                lost += nowLost;
                if (mDataDelegate != null)
                    mDataDelegate.lost(this, last, p.counter, nowLost);
            }
            if (p.isValid()) {
                if (mDataDelegate != null)
                    mDataDelegate.received(this, p);
                last = p.counter;
                ok++;
            }
            else {
                invalidChecksum++;
                Log.d(TAG, "invalidChecksum" + invalidChecksum);
            }
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
        void received(EXLs3Manager sender, Packet p);
        void lost(EXLs3Manager sender, int from, int to, int howMany);
    }

    public static class Packet {
        private final int m;
        public final long receptionTime;
        public final int counter;
        public final PacketType type;
        public final int ax, ay, az, gx, gy, gz, mx, my, mz, q1, q2, q3, q4, vbatt;
        public final int checksum_received, checksum_actual;

        public boolean isValid() {
            return checksum_received == checksum_actual;
        }

        public int getMaxPacketCounter() {
            return m;
        }

        public int lostFromPreviousCounter(int previousCounter) {
            return counter - previousCounter + (counter > previousCounter ? 0 : m) - 1;
        }

        public Packet(long receptionTime, PacketType type, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int q1, int q2, int q3, int q4, int vbatt, byte checksum_received, byte checksum_actual) {
            this.receptionTime = receptionTime;
            this.counter = counter;
            this.type = type;
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.gx = gx;
            this.gy = gy;
            this.gz = gz;
            this.mx = mx;
            this.my = my;
            this.mz = mz;
            this.q1 = q1;
            this.q2 = q2;
            this.q3 = q3;
            this.q4 = q4;
            this.vbatt = vbatt;
            this.checksum_received = checksum_received;
            this.checksum_actual = checksum_actual;
            this.m = (type == PacketType.AGMQB ? 10000 : 0xFF);
        }

        public static Packet parsePacket(long receptionTime, byte[] bytes)  {
            PacketType type;
            int counter;
            int ax;
            int ay;
            int az;
            int gx;
            int gy;
            int gz;
            int mx;
            int my;
            int mz;
            int q1;
            int q2;
            int q3;
            int q4;
            int vbatt;
            byte checksum_received;
            byte checksum_actual;

            if (bytes[0] != 0x20)
                return null;

            if (bytes[1] == PacketType.RAW.id)
                type = PacketType.RAW;
            else if (bytes[1] == PacketType.calib.id)
                type = PacketType.calib;
            else if (bytes[1] == PacketType.AGMB.id)
                type = PacketType.AGMB;
            else if (bytes[1] == PacketType.AGMQB.id)
                type = PacketType.AGMQB;
            else
                return null;

            int u = 2;

            counter = (bytes[u++] & 0xFF);
            if (bytes[1] == PacketType.AGMQB.id)
                counter += (bytes[u++]& 0xFF) * 0x100;
            ax = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            ay = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            az = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            gx = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            gy = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            gz = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            mx = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            my = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            mz = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            q1 = q2 = q3 = q4 = vbatt = 0;
            if (bytes[1] == PacketType.AGMQB.id) {
                q1 = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
                q2 = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
                q3 = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
                q4 = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
             vbatt = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
           } else if (bytes[1] == PacketType.AGMB.id)
             vbatt = (bytes[u++] & 0xFF) + (bytes[u++] & 0xFF) * 0x100;
            checksum_received = bytes[u++];
            byte ck = 0;
            for (int i = 0; i < u - 1; i++)
                ck += bytes[i];
            checksum_actual = ck;

            return new Packet(receptionTime, type, counter, ax, ay, az, gx, gy, gz, mx, my, mz, q1, q2, q3, q4, vbatt, checksum_received, checksum_actual);
        }
    }

    public enum PacketType {
        AGMQB((byte)0x9f, 33),
        AGMB((byte)0x97, 25),
        RAW((byte)0x0A, 22),
        calib((byte)0x0B, 22);

        final byte id;
        final int bytes;

        PacketType(byte id, int bytes) {
            this.id = id;
            this.bytes = bytes;
        }
    }
}