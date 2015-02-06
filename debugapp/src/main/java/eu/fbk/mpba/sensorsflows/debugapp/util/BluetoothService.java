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
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("NewApi")
public class BluetoothService {
    // TODO Parse the A G M Q B packet!!!

    public static interface DataDelegate {
        void receive(BluetoothService sender, Packet p);
    }

    public static interface StatusDelegate {
        void idle(BluetoothService sender);
        void listening(BluetoothService sender);
        void connecting(BluetoothService sender, BluetoothDevice device, boolean secureMode);
        void connected(BluetoothService sender, String deviceName);
        void connectionFailed(BluetoothService sender);
        void connectionLost(BluetoothService sender);
    }

    public static class Packet {
        public long receptionTime;
        public int counter;
        public int ax,ay,az,gx,gy,gz,mx,my,mz;
        public long checksum_received, checksum_actual;

        public boolean isValid() {
            return checksum_received == checksum_actual;
        }

        public Packet (long receptionTime, int counter, int ax, int ay, int az, int gx, int gy, int gz, int mx, int my, int mz, long checksum_received, long checksum_actual) {
            this.receptionTime = receptionTime;
            this.counter = counter;
            this.ax = ax;
            this.ay = ay;
            this.az = az;
            this.gx = gx;
            this.gy = gy;
            this.gz = gz;
            this.mx = mx;
            this.my = my;
            this.mz = mz;
            this.checksum_received = checksum_received;
            this.checksum_actual = checksum_actual;
        }
    }

    // Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

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

    public enum BTSrvState {
        IDLE,          // we're doing nothing
        LISTENING,        // now listening for incoming connections
        CONNECTING,    // now initiating an outgoing connection
        CONNECTED,     // now connected to a remote device
        DISCONNECTED   // Disconnected from device
    }

    public int packetsReceived = 0;
    public int packetCounterTotal = 0;
    public int lostPackets = 0;

    public long startStreamingTime = 0;

    /**
     * Constructor. Prepares a new BluetoothChat session with the default BluetoothAdapter.
     *
     */
    public BluetoothService(StatusDelegate statusDelegate, DataDelegate dataDelegate) {
        this(statusDelegate, dataDelegate, BluetoothAdapter.getDefaultAdapter());
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     */
    public BluetoothService(StatusDelegate statusDelegate, DataDelegate dataDelegate, BluetoothAdapter adapter) {
        mDataDelegate = dataDelegate;
        mStatusDelegate = statusDelegate;
        mAdapter = adapter;
        setState(BTSrvState.IDLE);
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(BTSrvState state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
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
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

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

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D) Log.d(TAG, "connect to: " + device);

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
        mConnectThread.start();
        setState(BTSrvState.CONNECTING);
        if (mStatusDelegate != null)
            mStatusDelegate.connecting(this, device, secure);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    protected synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if (D) Log.d(TAG, "connected, Socket Type:" + socketType);

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
        mConnectedThread = new ConnectedThread(socket, socketType);
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
        if (D) Log.d(TAG, "stop");

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

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
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
        BluetoothService.this.stop();
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
        BluetoothService.this.start();
    }

    public void sendStart() {
        String message = "==";
        startStreamingTime = android.os.SystemClock.elapsedRealtime();
        write(message.getBytes());
    }

    public void sendStop() {
        String message = "::";
        write(message.getBytes());
    }

    public void sendStartLog(int trialID, int patientID) {
        String message = "% SET PATIENTID " + trialID + " " + patientID + "\r\n  - -";
        write(message.getBytes());
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
            if (D) Log.d(TAG, "Socket Type: " + mSocketType +
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
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
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
            if (D) Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            if (D) Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
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
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
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
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
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

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
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

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            Log.i(TAG, "++ SET bootNanos");

            long bootNanos = System.currentTimeMillis() * 1000000 - System.nanoTime();

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    byte[] buffer = new byte[1024];

                    // Read from the InputStream
                    //noinspection ResultOfMethodCallIgnored
                    mmInStream.read(buffer);
                    long receptionTime = System.nanoTime() + bootNanos;

                    // Send the obtained bytes to the DataDelegate
                    int b_read, b_read_aux, ct_prev = 0;

                    while (mState == BTSrvState.CONNECTED) {
                        try {
                            // Read from the InputStream
                            int pointer = 0;

                            b_read = mmInStream.read();

                            if (b_read == 0x20) {

                                b_read_aux = mmInStream.read();

                                if (b_read_aux == 0x0A || b_read_aux == 0x0B) {
                                    try {
                                        buffer[pointer++] = (byte) b_read;
                                        buffer[pointer++] = (byte) b_read_aux;
                                        while (pointer < 22) {
                                            b_read = mmInStream.read();
                                            buffer[pointer++] = (byte) b_read;
                                        }

                                        if (buffer[0] == 0x20 && (buffer[1] == 0x0A || buffer[1] == 0x0B)) {

                                            Packet p = new Packet(
                                                    receptionTime,
                                                    ((int) buffer[2] & 0xFF),
                                                    (short) ((buffer[3] & 0xFF) + ((buffer[4] & 0xFF) * 256)),
                                                    (short) ((buffer[5] & 0xFF) + ((buffer[6] & 0xFF) * 256)),
                                                    (short) ((buffer[7] & 0xFF) + ((buffer[8] & 0xFF) * 256)),

                                                    (short) ((buffer[9] & 0xFF) + ((buffer[10] & 0xFF) * 256)),
                                                    (short) ((buffer[11] & 0xFF) + ((buffer[12] & 0xFF) * 256)),
                                                    (short) ((buffer[13] & 0xFF) + ((buffer[14] & 0xFF) * 256)),

                                                    (short) ((buffer[16] & 0xFF) + ((buffer[15] & 0xFF) * 256)),
                                                    (short) ((buffer[18] & 0xFF) + ((buffer[17] & 0xFF) * 256)),
                                                    (short) ((buffer[20] & 0xFF) + ((buffer[19] & 0xFF) * 256)),
                                                    ((int) buffer[21] & 0xFF), 0);

                                            long checksum_cmp = 0;
                                            for (int j = 0; j < 21; j++)
                                                checksum_cmp = checksum_cmp ^ ((int) buffer[j] & 0xFF);

                                            p.checksum_actual = checksum_cmp;

                                            lostPackets += (p.counter - ct_prev + 255) % 256;
                                            packetCounterTotal += (p.counter - ct_prev + 256) % 256;
                                            packetsReceived++;
                                            ct_prev = p.counter;

                                            if (mDataDelegate != null)
                                                mDataDelegate.receive(BluetoothService.this, p);
                                        }
                                    } catch (IOException e1) {
                                        lostPackets++;
                                        Log.e(TAG, "Unexpected packet format (short)");
                                    }
                                }
                                else
                                    Log.e(TAG, "Unexpected packet format (" + Integer.toHexString(b_read) + " and " + Integer.toHexString(b_read_aux) +")");
                            }
                            else
                                Log.e(TAG, "Unexpected packet format (" + Integer.toHexString(b_read) +")");

                        } catch (IOException e) {
                            Log.e(TAG, "disconnected", e);
                            connectionLost();
                            break;
                        }
                    }


                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
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