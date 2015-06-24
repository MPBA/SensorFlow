package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.EXLs3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public abstract class EXLs3Receiver {

    // Debug
    private static final String TAG = EXLs3Receiver.class.getSimpleName();

    // UUID for rfcomm connection
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private BTSrvState mState = BTSrvState.IDLE;
    private BluetoothSocket mSocket;
    private InputStream mInput;
    private OutputStream mOutput;
    private Thread mDispatcher;
    protected final BluetoothAdapter mAdapter;
    protected final StatusDelegate mStatusDelegate;
    protected final BluetoothDevice mDevice;
    private boolean dispatch = true;

    public EXLs3Receiver(StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mStatusDelegate = statusDelegate;
        mDevice = device;
        mAdapter = adapter;
        mDispatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                dispatch();
            }
        });
    }

    // Operation

    private boolean startPending = false;

    protected void connect() {
        dispatch = true;
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
                    mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.IO_STREAMS_ERROR);
                close();
            }
        }
        else {
            // Connection Failed
            setState(BTSrvState.DISCONNECTED);
            if (mStatusDelegate != null)
                mStatusDelegate.disconnected(this, mSocket == null
                        ? StatusDelegate.DisconnectionCause.IO_SOCKET_ERROR
                        : StatusDelegate.DisconnectionCause.DEVICE_NOT_FOUND);
            close();
        }
    }

    public void startStream() {
        if (mOutput != null && mSocket.isConnected()) {
            try {
                mOutput.write(new byte[] { 0x3D, 0x3D });
                Log.v("", "== <--- sent");
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
                Log.v("", ":: <--- sent");
            } catch (IOException ignored) { }
        }
        else
            startPending = false;
    }

    protected void close() {
        stopStream();
        setState(BTSrvState.DISCONNECTED); // Need this to handle the IOException
        dispatch = false;
        try {
            if(mInput != null) {
                mInput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(mOutput != null) {
                mOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutput = null;
        mSocket = null;
    }

    private void dispatch() {
        // In uno dei percorsi scarta bytes,
        // utilizzabile solo per controllare il packet counter
        Log.i(TAG, "Started dispatching...");
        int lostBytes = 0;
        try {
            int i;
            byte[] pack = new byte[33];
            try {
                // Should solve the ignored start stream command issue.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.d(TAG, "Dispatch interrupted on anti-ignore sleep.");
            }
            while (dispatch) {
                i = 0;
                pack[i++] = (byte) mInput.read();
                if (pack[0] == 0x20) {
                    if (lostBytes != 0) {
                        Log.d(TAG, "LostBytes:" + lostBytes);
                        lostBytes = 0;
                    }
                    pack[i++] = (byte) mInput.read();
                    if (pack[1] == EXLs3Manager.PacketType.AGMQB.id) {
                        // TODO Try this i += mInput.read(pack, i, 31);
                        while (i < EXLs3Manager.PacketType.AGMQB.bytes)
                            pack[i++] = (byte) mInput.read();
                    } else if (pack[1] == EXLs3Manager.PacketType.AGMB.id) {
                        // TODO Try this i += mInput.read(pack, i, 23);
                        while (i < EXLs3Manager.PacketType.AGMB.bytes)
                            pack[i++] = (byte) mInput.read();
                    } else if (pack[1] == EXLs3Manager.PacketType.RAW.id
                            || pack[1] == EXLs3Manager.PacketType.calib.id) {
                        // TODO Try this i += mInput.read(pack, i, 20);
                        while (i < EXLs3Manager.PacketType.RAW.bytes)
                            pack[i++] = (byte) mInput.read();
                    }
                    received(pack, i);
                }
                else {
                    lostBytes++;
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "+++ Disconnection: " + e.getMessage() + " <- " + e.getClass().getName());
            switch (e.getMessage()) {
                case "Operation Canceled":
                case "bt socket closed, read return: -1":
                    if (!dispatch) {
                        Log.d(TAG, "Regular bt socket closed");
                    } else {
                        Log.e(TAG, "Connection lost", e);
                        setState(BTSrvState.DISCONNECTED);
                        if (mStatusDelegate != null)
                            mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.CONNECTION_LOST);
                    }
                    break;
                case "Software caused connection abort":
                    Log.e(TAG, "Software caused connection abort", e);
                    break;
                default:
                    Log.e(TAG, "Unmanageable disconnection", e);
                    break;
            }
        }
        Log.d(TAG, "Dispatch thread end");
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
                        Log.e(TAG, "closed" );
                        break;
                    case "Bluetooth is off":
                        Log.e(TAG, "BT off" );
                        break;
                    case "Device or resource busy":
                        Log.e(TAG, "Busy" );
                        break;
                    case "Host is down":
                        Log.e(TAG, "Host is down" );
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

    private static BluetoothSocket createSocket(final BluetoothDevice device) {
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

    // To implement

    protected abstract void received(byte[] buffer, int bytes);

    // Subclasses

    public static interface StatusDelegate {

        public final int
                READY = 0,
                CONNECTING = 1,
                CONNECTED = 2,
                DISCONNECTED = 4;

        void connecting(EXLs3Receiver sender, BluetoothDevice device, boolean secureMode);
        void connected(EXLs3Receiver sender, String deviceName);
        void disconnected(EXLs3Receiver sender, DisconnectionCause cause);

        public enum DisconnectionCause {

            DEVICE_NOT_FOUND(8),
            IO_STREAMS_ERROR(16),
            IO_SOCKET_ERROR(24),
            CONNECTION_LOST(32),
            WRONG_PACKET_TYPE(40),
            OTHER(48);

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