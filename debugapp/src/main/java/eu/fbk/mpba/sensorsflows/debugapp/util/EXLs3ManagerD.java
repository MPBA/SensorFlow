package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("NewApi")
public class EXLs3ManagerD {

    public static interface DataDelegate {
        void receive(EXLs3ManagerD sender, Packet p);
    }

    public static interface StatusDelegate {
        void idle(EXLs3ManagerD sender);
        void listening(EXLs3ManagerD sender);
        void connecting(EXLs3ManagerD sender, BluetoothDevice device, boolean secureMode);
        void connected(EXLs3ManagerD sender, String deviceName);
        void connectionFailed(EXLs3ManagerD sender);
        void connectionLost(EXLs3ManagerD sender);
    }

    public static class Packet {
        public final long receptionTime;
        public final int counter;
        public final PacketType type;
        public final int ax,ay,az,gx,gy,gz,mx,my,mz,q1,q2,q3,q4,vbatt;
        public final int checksum_received, checksum_actual;

       /* public boolean isValid() {
            return checksum_received == checksum_actual;
        }*/

        public Packet (long receptionTime, InputStream s) throws IOException {
            this.receptionTime = receptionTime;

            int[] b = new int[40];
            int u = 0, x;
            b[u++] = 0x20;

            if ((x = (byte)s.read()) != 0x20)
                b[u++] = (byte)x;

            if (b[1] == PacketType.RAW.id || b[1] == PacketType.AGMQB.id) {
                  type = b[1] == PacketType.RAW.id ? PacketType.RAW : PacketType.AGMQB;
                  counter = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       ax = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       ay = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       az = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       gx = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       gy = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       gz = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       mx = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       my = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       mz = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                if (b[1] == PacketType.AGMQB.id) {
                       q1 = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       q2 = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       q3 = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                       q4 = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                    vbatt = (b[u++] = (s.read() & 0xFF)) + (b[u++] = (s.read() & 0xFF)) * 0x100;
                }
                else
                    q1 = q2 = q3 = q4 = vbatt = 0;
                checksum_received = (b[u++] = (s.read() & 0xFF));
                byte ck = 0;
                for (int i = 0; i < u - 1; i++)
                    ck ^= b[i];
                checksum_actual = ck;
            }
            else
                throw new UnsupportedEncodingException("Packet type not supported " + Integer.toHexString(b[0]) + " " + Integer.toHexString(b[1]));
        }

        public Packet (long receptionTime, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int checksum_received, int checksum_actual) {
            this(receptionTime, PacketType.RAW, counter, ax, ay, az,gx, gy, gz, mx, my, mz, 0, 0, 0, 0, 0, checksum_received, checksum_actual);
        }

        public Packet (long receptionTime, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int q1, int q2, int q3, int q4, int checksum_received, int checksum_actual) {
            this(receptionTime, PacketType.AGMQB, counter, ax, ay, az,gx, gy, gz, mx, my, mz, q1, q2, q3, q4, 0, checksum_received, checksum_actual);
        }

        public Packet (long receptionTime, PacketType type, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int q1, int q2, int q3, int q4, int vbatt, int checksum_received, int checksum_actual) {
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
        }
    }

    public enum PacketType {
        AGMQB((byte)0x9f),
        RAW((byte)0x0A);

        byte id;

        PacketType(byte id) {
            this.id = id;
        }
    }

    public enum BTSrvState {
        IDLE,          // we're doing nothing
        LISTENING,     // now listening for incoming connections
        CONNECTING,    // now initiating an outgoing connection
        CONNECTED,     // now connected to a remote device
        DISCONNECTED   // Disconnected from device
    }

    // Debugging
    private static final String TAG = EXLs3ManagerD.class.getSimpleName();

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = EXLs3ManagerD.class.getSimpleName() + "Secure";
    private static final String NAME_INSECURE = EXLs3ManagerD.class.getSimpleName() + "Insecure";

    // Unique UUID for this application
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private BluetoothDevice mDevice;
    private final BluetoothAdapter mAdapter;
    private final DataDelegate mDataDelegate;
    private final StatusDelegate mStatusDelegate;
    private BTSrvState mState;

    public EXLs3ManagerD(StatusDelegate statusDelegate, DataDelegate dataDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mDataDelegate = dataDelegate;
        mStatusDelegate = statusDelegate;
        mDevice = device;
        mAdapter = adapter;
        setState(BTSrvState.IDLE);
        tryConnect();
    }

    private synchronized void setState(BTSrvState state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        if (mStatusDelegate != null)
            switch (state) {
                case IDLE:
                    mStatusDelegate.idle(this);
                    break;
                case LISTENING:
                    mStatusDelegate.listening(this);
                    break;
            }
    }

    public synchronized BTSrvState getState() {
        return mState;
    }

    public static BluetoothSocket createSocket(final BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", int.class);
            socket = (BluetoothSocket)m.invoke(device, 1);
        }
        catch (NoSuchMethodException ignore) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                Log.e(TAG, "IOException to get the socket", e);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to get the socket", e);
        }
        return socket;
    }

    private boolean tryConnect() {
        if (mDevice != null) {
            Log.d(TAG, "TryConnect to " + mDevice.getName() + " MAC:" + mDevice.getAddress());
            BluetoothSocket socket = createSocket(mDevice);
            mAdapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Unable to connect to the device", e);
                return false;
            }
            Log.i(TAG, "Connected to " + mDevice.getName() + " MAC:" + mDevice.getAddress());
            return true;
        }
        else
            return false;
    }
}