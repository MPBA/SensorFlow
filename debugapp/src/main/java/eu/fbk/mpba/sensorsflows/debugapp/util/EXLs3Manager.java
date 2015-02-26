package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("NewApi")
public class EXLs3Manager {

    // Debug
    private static final String TAG = EXLs3Manager.class.getSimpleName();

    // UUID for rfcomm connection
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final int MAX_WRONG_PACKETS = 10;

    // Member fields
    private BluetoothDevice mDevice;
    private final BluetoothAdapter mAdapter;
    private final DataDelegate mDataDelegate;
    private final StatusDelegate mStatusDelegate;
    private BTSrvState mState;
    private BluetoothSocket mSocket;
    protected InputStream mInput;
    protected OutputStream mOutput;
    protected Thread mDispatcher;

    public EXLs3Manager(StatusDelegate statusDelegate, DataDelegate dataDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mDataDelegate = dataDelegate;
        mStatusDelegate = statusDelegate;
        mDevice = device;
        mAdapter = adapter;
        setState(BTSrvState.IDLE);
    }

    // Operation

    public void connect() {
        setState(BTSrvState.CONNECTING);
        if (mStatusDelegate != null)
            mStatusDelegate.connecting(this, mDevice, false); // TODO 8 Not only insecure
        if (tryConnect()) {     // Acts as a reset
            // Connection Established
            setState(BTSrvState.CONNECTED);
            if (mStatusDelegate != null)
                mStatusDelegate.connected(this, mDevice.getAddress() + "-" + mDevice.getName());
            // Get io streams
            try {
                mInput = mSocket.getInputStream();
                mOutput = mSocket.getOutputStream();
                mDispatcher = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dispatch();
                    }
                });
                mDispatcher.start();
            } catch (IOException e) {
                Log.e(TAG, "Trying to get the I/O bluetooth streams", e);
                // Connection Failed
                setState(BTSrvState.DISCONNECTED);
                if (mStatusDelegate != null)
                    mStatusDelegate.disconnected(this, StatusDelegate.Cause.IO_STREAMS_ERROR);
            }
        }
        else {
            // Connection Failed
            setState(BTSrvState.DISCONNECTED);
            if (mStatusDelegate != null)
                mStatusDelegate.disconnected(this, mSocket == null
                        ? StatusDelegate.Cause.IO_SOCKET_ERROR
                        : StatusDelegate.Cause.DEVICE_NOT_FOUND);
        }
    }

    private void keepAlive() {
        startStream();
    }

    protected boolean startPending = false;

    public void startStream() {
        if (mOutput != null) {
            try {
                mOutput.write("==".getBytes());
            } catch (IOException e) {
                throw new UnsupportedOperationException("Strange error", e);
            }
        }
        else
            startPending = true;
    }

    public void stopStream() {
        if (mOutput != null) {
            try {
                mOutput.write("::".getBytes());
            } catch (IOException ignored) { }
        }
        else
            startPending = false;
    }

    private long last = 0;
    private int readByte() throws IOException {
        if (last - System.nanoTime() > 5000_000000L) {
            keepAlive();
            last = System.nanoTime();
        }
        return mInput.read();
    }

    private long lostBytes = 0;
    public void dispatch() {
        int lastC = -1, wrongTypePackets = 0;
        boolean dispatch = true;
        // Keep wrongPacketType to the InputStream while connected
        Packet p;
        try {
            while (dispatch) {
                if (mInput.available() < 10)
                    Thread.sleep(3);
                if (mInput.available() > 0) {
                    while (readByte() != 0x20) {
                        lostBytes++;
                    }
                    if (lostBytes > 0) {
                        Log.v(TAG, lostBytes + " lostBytes");
                        lostBytes = 0;
                    }
                    p = Packet.parsePacket(System.nanoTime(), mInput);
                    if (p == null) {
                        if (wrongTypePackets++ > MAX_WRONG_PACKETS) {
                            // Closing connection and everything else
                            setState(BTSrvState.DISCONNECTED);
                            if (mStatusDelegate != null)
                                mStatusDelegate.wrongPacketType(this);
                            close();
                            dispatch = false;
                        }
                    } else {
                        if ((lastC + 1) % 10000 != p.counter)
                            Log.v(TAG, "l:" + (lastC + 1) + "c:" + (lastC = p.counter));
                        if (mDataDelegate != null)
                            mDataDelegate.receive(this, p);
                    }
                }
            }
        } catch (InterruptedException ignore) {
        } catch (IOException e) {
            Log.e(TAG, "Forced disconnection", e);
            // Connection Lost
            setState(BTSrvState.DISCONNECTED);
            if (mStatusDelegate != null)
                mStatusDelegate.disconnected(this, StatusDelegate.Cause.CONNECTION_LOST);
        }
    }

    public void close() {
        if (mDispatcher != null)
            try {
                while (mDispatcher.isAlive()) {
                    mDispatcher.interrupt();
                    mDispatcher.join(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        try {
            if(mInput != null)
                mInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(mOutput != null)
                mOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(mSocket != null)
                mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean tryConnect() {
        if (mDevice != null) {
            String devInfo = mDevice.getAddress() + "-" + mDevice.getName();
            Log.d(TAG, "++ TryConnect " + devInfo);
            mSocket = createSocket(mDevice);
            mAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                Log.i(TAG, "+++ Connected " + devInfo);
                if (startPending) {
                    Log.i(TAG, "++ startPending, starting " + devInfo);
                    startStream();
                }
                return true;
            } catch (IOException e) {
                Log.e(TAG, "+++++ connect() failed " + devInfo, e);
                return false;
            }
        }
        else
            return false;
    }

    // Status

    public BTSrvState getState() {
        return mState;
    }

    private void setState(BTSrvState state) {
        Log.v(TAG, "+Status " + mState + " -> " + state);
        mState = state;
    }

    // Util

    protected static BluetoothSocket createSocket(final BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            Method m = device.getClass().getMethod("createRfcommSocket", int.class);
            socket = (BluetoothSocket)m.invoke(device, 1);
        }
        catch (NoSuchMethodException ignore) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "IOException trying to create the socket", e);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to create the socket", e);
        }
        return socket;
    }

    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object) "counter", "ax", "ay", "az", "gx", "gy", "gz", "mx", "my", "mz", "q1", "q2", "q3", "q4", "vbatt");
    }

    // Subclasses

    public static interface DataDelegate {
        void receive(EXLs3Manager sender, Packet p);
    }

    public static interface StatusDelegate {
        void wrongPacketType(EXLs3Manager sender);
        void connecting(EXLs3Manager sender, BluetoothDevice device, boolean secureMode);
        void connected(EXLs3Manager sender, String deviceName);
        void disconnected(EXLs3Manager sender, Cause cause);

        public enum Cause {
            DEVICE_NOT_FOUND,
            IO_STREAMS_ERROR, IO_SOCKET_ERROR, CONNECTION_LOST
        }
    }

    public static class Packet {
        public final long receptionTime;
        public final int counter;
        public final PacketType type;
        public final int ax,ay,az,gx,gy,gz,mx,my,mz,q1,q2,q3,q4,vbatt;
        public final int checksum_received, checksum_actual;

        public boolean isValid() {
            return checksum_received == checksum_actual;
        }

        public Packet (long receptionTime, PacketType type, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int q1, int q2, int q3, int q4, int vbatt, int checksum_received, int checksum_actual) {
            this.receptionTime = receptionTime;
            this.counter = counter; this.type = type;
            this.ax = ax; this.ay = ay; this.az = az;
            this.gx = gx; this.gy = gy; this.gz = gz;
            this.mx = mx; this.my = my; this.mz = mz;
            this.q1 = q1; this.q2 = q2; this.q3 = q3; this.q4 = q4;
            this.vbatt = vbatt;
            this.checksum_received = checksum_received;
            this.checksum_actual = checksum_actual;
        }

        public static Packet parsePacket(long receptionTime, InputStream s) throws IOException {
            PacketType type; int counter;
            int ax; int ay; int az;
            int gx; int gy; int gz;
            int mx; int my; int mz;
            int q1; int q2; int q3; int q4;
            int vbatt; int checksum_received; int checksum_actual;

            int[] b = new int[40];
            int u = 0, x;
            b[u++] = 0x20;

            if ((x = (byte) s.read()) != 0x20)
                b[u++] = (byte) x;

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
                } else
                    q1 = q2 = q3 = q4 = vbatt = 0;
                checksum_received = (b[u++] = (s.read() & 0xFF));
                byte ck = 0;
                for (int i = 0; i < u - 1; i++)
                    ck ^= b[i];
                checksum_actual = ck;

                return new Packet(receptionTime, type, counter, ax, ay, az, gx, gy, gz, mx, my, mz, q1, q2, q3, q4, vbatt, checksum_received, checksum_actual);
            } else
                return null;
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
        CONNECTING,    // now initiating an outgoing connection
        CONNECTED,     // now connected to a remote device
        DISCONNECTED   // disconnected from device, error or
    }
}