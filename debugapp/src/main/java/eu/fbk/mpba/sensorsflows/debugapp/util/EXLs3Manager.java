package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("NewApi")
public class EXLs3Manager {

    public static interface DataDelegate {
        void receive(EXLs3Manager sender, Packet p);
    }

    public static interface StatusDelegate {
        void idle(EXLs3Manager sender);
        void listening(EXLs3Manager sender);
        void connecting(EXLs3Manager sender, BluetoothDevice device, boolean secureMode);
        void connected(EXLs3Manager sender, String deviceName);
        void connectionFailed(EXLs3Manager sender);
        void connectionLost(EXLs3Manager sender);
    }

    public static class Packet {
        public final long receprionNanos;
        public final int counter;
        public final PacketType type;
        public final int ax,ay,az,gx,gy,gz,mx,my,mz,q1,q2,q3,q4,vbatt;
        public final int checksum_received, checksum_actual;

       /* public boolean isValid() {
            return checksum_received == checksum_actual;
        }*/

        public Packet (long receprionNanos, InputStream s) throws IOException {
            this.receprionNanos = receprionNanos;

            int[] b = new int[40];
            int u = 0, x;
            b[u++] = 0x20;

            if ((x = (byte)s.read()) != 0x20)
                b[u++] = (byte)x;

            if (b[1] == PacketType.calib.id || b[1] == PacketType.RAW.id || b[1] == PacketType.AGMQB.id) {
                  type = PacketType.parse(b[1]);
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

        public Packet (long receprionNanos, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int checksum_received) {
            this(receprionNanos, PacketType.RAW, counter, ax, ay, az,gx, gy, gz, mx, my, mz, 0, 0, 0, 0, 0, checksum_received);
        }

        public Packet (long receprionNanos, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int q1, int q2, int q3, int q4, int checksum_received) {
            this(receprionNanos, PacketType.AGMQB, counter, ax, ay, az,gx, gy, gz, mx, my, mz, q1, q2, q3, q4, 0, checksum_received);
        }
        
        public Packet (long receprionNanos, PacketType type, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, int q1, int q2, int q3, int q4, int vbatt, int checksum_received) {
            this.receprionNanos = receprionNanos;
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
            this.checksum_actual = 0; // TODO Calc
        }
    }

    public enum PacketType {
        AGMQB((byte)0x9f),
        RAW((byte)0x0A),
        calib((byte)0x0B);

        byte id;

        PacketType(byte id) {
            this.id = id;
        }

        static PacketType parse(int id) {
            switch (id & 0xFF) {
                case 0x0A:
                    return RAW;
                case 0x0B:
                    return calib;
                case 0x9F:
                    return AGMQB;
                default:
                    return null;
            }
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
    private static final String TAG = EXLs3Manager.class.getSimpleName();

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = EXLs3Manager.class.getSimpleName() + "Secure";
    private static final String NAME_INSECURE = EXLs3Manager.class.getSimpleName() + "Insecure";

    // Unique UUID for this application
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final DataDelegate mDataDelegate;
    private final StatusDelegate mStatusDelegate;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BTSrvState mState;

    public int lostBytes = 0;

    public long startStreamingTime = 0;

    /**
     * Constructor. Prepares a new BluetoothChat session with the default BluetoothAdapter.
     *
     */
    public EXLs3Manager(StatusDelegate statusDelegate, DataDelegate dataDelegate) {
        this(statusDelegate, dataDelegate, BluetoothAdapter.getDefaultAdapter());
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     */
    public EXLs3Manager(StatusDelegate statusDelegate, DataDelegate dataDelegate, BluetoothAdapter adapter) {
        mDataDelegate = dataDelegate;
        mStatusDelegate = statusDelegate;
        mAdapter = adapter;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
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

    /**
     * Return the current connection state.
     */
    public synchronized BTSrvState getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    protected synchronized void start() {
        Log.d(TAG, "start()");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(BTSrvState.LISTENING);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == BTSrvState.CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        setState(BTSrvState.CONNECTING);
        if (mStatusDelegate != null)
            mStatusDelegate.connecting(this, device, secure);
        mConnectThread.run();
    }

    protected synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, String socketType) {
        Log.d(TAG, "connected() Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(this, socket, socketType);
        mConnectedThread.start();

        setState(BTSrvState.CONNECTED);

        // Send the name of the connected device back to the UI Activity
        if (mStatusDelegate != null)
            mStatusDelegate.connected(this, device.getName());
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop()");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(BTSrvState.IDLE);
    }

    protected void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState == BTSrvState.CONNECTED)
                r = mConnectedThread;
            else
                return;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity

        if (mStatusDelegate != null)
            mStatusDelegate.connectionFailed(this);

        setState(BTSrvState.DISCONNECTED);
        // WAS Start the service over to restart listening mode
        // Makes sense stopping everything as it has been notified to the user
        EXLs3Manager.this.stop();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        if (mStatusDelegate != null)
            mStatusDelegate.connectionLost(this);

        setState(BTSrvState.DISCONNECTED);
        // Start the service over to restart listening mode
        EXLs3Manager.this.start();
    }

    public void sendStart() {
        startStreamingTime = android.os.SystemClock.elapsedRealtime();
        write("==".getBytes());
    }

    public void sendStop() {
        write("::".getBytes());
    }

    public void sendStartLog(int trialID, int patientID) {
        write("--".getBytes());
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            // Listen to the server socket if we're not connected
            while (mState != BTSrvState.CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    if (mmServerSocket != null)
                        socket = mmServerSocket.accept();
                    else {
                        Log.e(TAG, "Null mmServerSocket");
                        return;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + " accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (EXLs3Manager.this) {
                        switch (mState) {
                            case LISTENING:
                            case CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case IDLE:
                            case CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + " create() failed", e);
            }
            Log.d(TAG, "BTSocket " + mSocketType + " created");
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                // Reset the ConnectThread because we're done
                synchronized (EXLs3Manager.this) {
                    mConnectThread = null;
                }

                // Start the connected thread
                connected(mmSocket, mmDevice, mSocketType);
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                Log.e(TAG, "unable to connect() " + mSocketType, e);
                connectionFailed();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final EXLs3Manager caller;

        public ConnectedThread(EXLs3Manager caller, BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            this.caller = caller;
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        private long last = 0;
        private int readByte() throws IOException {
            if (last - System.nanoTime() > 1900_000000) {
                sendStart();
                last = System.nanoTime();
            }
            return mmInStream.read();
        }

        public void run() {
            Log.i(TAG, "++BEGIN reading thread");

            int lastC = -1;

            // Keep listening to the InputStream while connected
            while (true) {
                Packet p;
                while (caller.mState == BTSrvState.CONNECTED) {
                    try {

                        while (readByte() != 0x20) {
                            caller.lostBytes++;
                        }
                        try {
                            p = new Packet(System.nanoTime(), mmInStream);
                            if ((lastC + 1) % 10000 != p.counter)
                                Log.v("@@@", "l:" + lastC + "c:" + (lastC = p.counter));

                        } catch (UnsupportedEncodingException e) {
                            p = null;
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        caller.connectionLost();
                        break;
                    }
                    if (p != null)
                        caller.mDataDelegate.receive(caller, p);
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}