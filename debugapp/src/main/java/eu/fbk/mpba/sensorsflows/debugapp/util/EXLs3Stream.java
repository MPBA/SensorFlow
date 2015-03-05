package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class EXLs3Stream extends InputStream {

    // Debug
    private static final String TAG = EXLs3Stream.class.getSimpleName();

    // UUID for rfcomm connection
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private BluetoothDevice mDevice;
    private final BluetoothAdapter mAdapter;
    private final StatusDelegate mStatusDelegate;
    private BTSrvState mState = BTSrvState.IDLE;
    private BluetoothSocket mSocket;
    protected InputStream mInput;
    protected OutputStream mOutput;

    public EXLs3Stream(StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mStatusDelegate = statusDelegate;
        mDevice = device;
        mAdapter = adapter;
    }

    // Operation

    public void connect() {
        if (!mAdapter.isEnabled()) {
            try {
                mAdapter.enable();
            } catch (Exception e) {
                Log.e(TAG, "BT may be disabled", e);
            }
        }
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
            } catch (IOException e) {
                Log.e(TAG, "Trying to get the I/O bluetooth streams", e);
                // Connection Failed
                setState(BTSrvState.DISCONNECTED);
                if (mStatusDelegate != null)
                    mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.IO_STREAMS_ERROR);
            }
        }
        else {
            // Connection Failed
            setState(BTSrvState.DISCONNECTED);
            if (mStatusDelegate != null)
                mStatusDelegate.disconnected(this, mSocket == null
                        ? StatusDelegate.DisconnectionCause.IO_SOCKET_ERROR
                        : StatusDelegate.DisconnectionCause.DEVICE_NOT_FOUND);
        }
    }

    protected boolean startPending = false;

    public void startStream() {
        if (mOutput != null) {
            try {
                mOutput.write(new byte[] { 0x3D, 0x3D });
            } catch (IOException e) {
                throw new UnsupportedOperationException("Unexpected error!", e);
            }
        }
        else
            startPending = true;
    }

    public void stopStream() {
        if (mOutput != null) {
            try {
                mOutput.write(new byte[] { 0x3A, 0x3A });
            } catch (IOException ignored) { }
        }
        else
            startPending = false;
    }

    public void close() {
        stopStream();
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

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of the stream has been
     * reached. Blocks until one byte has been read, the end of the source
     * stream is detected or an exception is thrown.
     *
     * @throws java.io.IOException if the stream is closed or another IOException occurs.
     */
    @Override
    public int read() throws IOException {
        return mInput.read();
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
                    Log.i(TAG, "+ startPending: starting streaming " + devInfo);
                    startStream();
                }
                return true;
            } catch (IOException e) {
                Log.e(TAG, "+++++ connect() failed " + devInfo);
                switch (e.getMessage()) {
                    case "read failed, socket might closed or timeout, read ret: -1":
                        break;
                    case "Bluetooth is off":
                        break;
                    default:
                        Log.e(TAG, "Unmanageable connect() failed", e);
                        break;
                }
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

    protected void setState(BTSrvState state) {
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

    // Subclasses

    public static interface StatusDelegate {

        public final int
                READY = 0,
                CONNECTING = 1,
                CONNECTED = 2,
                DISCONNECTED = 4;

        void connecting(EXLs3Stream sender, BluetoothDevice device, boolean secureMode);
        void connected(EXLs3Stream sender, String deviceName);
        void disconnected(EXLs3Stream sender, DisconnectionCause cause);

        public enum DisconnectionCause {

            DEVICE_NOT_FOUND(8),
            IO_STREAMS_ERROR(16),
            IO_SOCKET_ERROR(24),
            CONNECTION_LOST(32),
            WRONG_PACKET_TYPE(40);

            public final int flag;

            DisconnectionCause(int v) { flag = v; }
        }
    }

    public enum BTSrvState {
        IDLE,          // we're doing nothing
        CONNECTING,    // now initiating an outgoing connection
        CONNECTED,     // now connected to a remote device
        DISCONNECTED   // disconnected from device, error or
    }
}