package eu.fbk.mpba.sensorsflows.plugins.inputs.axivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public abstract class Wax9Receiver {

    // Debug
    private static final String TAG = Wax9Receiver.class.getSimpleName();

    // UUID for rfcomm connection
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private BTSrvState mState = BTSrvState.IDLE;
    private BluetoothSocket mSocket;
    private SlipInputStream mInput;
    private OutputStream mOutput;
    private Thread mDispatcher;
    protected final BluetoothAdapter mAdapter;
    protected final StatusDelegate mStatusDelegate;
    protected final BluetoothDevice mDevice;
    private boolean dispatch = true;

    public Wax9Receiver(StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mStatusDelegate = statusDelegate;
        mDevice = device;
        mAdapter = adapter;
    }

    // Operation

    protected void connect(int retry) { // TODO 8 Using only the insecure mode
        if (getState() == BTSrvState.CONNECTED) {
            if (mStatusDelegate != null)
                mStatusDelegate.connected(this, mDevice.getAddress() + "-" + mDevice.getName());
        } else {
            dispatch = true;
            if (!mAdapter.isEnabled()) {
                try {
                    mAdapter.enable();
                } catch (Exception e) {
                    Log.e(TAG, "Can't enable BT! Getting over.", e);
                }
            }
            setState(BTSrvState.CONNECTING);
            if (mStatusDelegate != null)
                mStatusDelegate.connecting(this, mDevice, false);
            while (retry-- > 0) {
                if (mDevice != null) {
                    String devInfo = mDevice.getAddress() + "-" + mDevice.getName();
                    Log.d(TAG, devInfo + ": try connect");
                    mSocket = createSocket(mDevice);
                    if (mSocket == null) {
                        // Connection Failed
                        setState(BTSrvState.DISCONNECTED);
                        if (mStatusDelegate != null)
                            mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.OTHER);
                        closeConnection();
                    } else {
                        if (!mAdapter.cancelDiscovery())
                            Log.e(TAG, devInfo + ": cancelDiscovery failed, getting over");
                        try {
                            mSocket.connect();
                            Log.i(TAG, devInfo + ": connected");
                            // Ok
                            retry = 0; // EXIT
                            {
                                // Connection Established
                                setState(BTSrvState.CONNECTED);
                                if (mStatusDelegate != null)
                                    mStatusDelegate.connected(this, mDevice.getAddress() + "-" + mDevice.getName());
                                // Get io streams
                                try {
                                    Log.v(TAG, "Getting I/O streams");
                                    mInput = new SlipInputStream(mSocket.getInputStream(), true);
                                    mOutput = mSocket.getOutputStream();
                                    Log.v(TAG, "Got I/O streams");

                                    mDispatcher = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dispatch();
                                        }
                                    });
                                    Log.v(TAG, "Starting asynch dispatcher");
                                    mDispatcher.start();
                                    Log.v(TAG, "Stort");

                                } catch (IOException e) {
                                    Log.e(TAG, "Can't get I/O streams", e);
                                    // Connection Failed
                                    setState(BTSrvState.DISCONNECTED);
                                    if (mStatusDelegate != null)
                                        mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.IO_STREAMS_ERROR);
                                    closeConnection();
                                    retry = 0; // EXIT
                                }
                            }
                        } catch (IOException e) {
                            Log.e(TAG, devInfo + ": IOException, connect failure");
                            switch (e.getMessage()) {
                                case "read failed, socket might closed or timeout, read ret: -1":
                                    break; // switch, retry
                                case "Bluetooth is off":
                                    Log.e(TAG, e.getMessage());
                                    // Connection Failed
                                    setState(BTSrvState.DISCONNECTED);
                                    if (mStatusDelegate != null)
                                        mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.BLUETOOTH_IS_OFF);
                                    closeConnection();
                                    //retry = 0; // EXIT
                                    break;
                                case "Device or resource busy":
                                case "Host is down":
                                    Log.e(TAG, e.getMessage());
                                    // Connection Failed
                                    setState(BTSrvState.DISCONNECTED);
                                    if (mStatusDelegate != null)
                                        mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.DEVICE_NOT_FOUND);
                                    closeConnection();
                                    //retry = 0; // EXIT
                                    break;
                                default:
                                    Log.e(TAG, "Unrecognized connect failure", e);
                                    // Connection Failed
                                    setState(BTSrvState.DISCONNECTED);
                                    if (mStatusDelegate != null)
                                        mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.OTHER);
                                    closeConnection();
                                    //retry = 0; // EXIT
                                    break;
                            }
                        }
                    }
                } else {
                    // Connection Failed
                    setState(BTSrvState.DISCONNECTED);
                    if (mStatusDelegate != null)
                        mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.DEVICE_NOT_SET);
                    closeConnection();
                }
            }
        }
    }

    public void command(byte[] c) {
        if (mOutput != null && mSocket != null && mSocket.isConnected()) {
            try {
                mOutput.write(c);
                Log.v(TAG, "Query sent");
            } catch (IOException e) {
                Log.e(TAG, "Query error", e);
            }
        }
    }

    protected void closeConnection() {
        Log.d(TAG, "close");
        dispatch = false;
        command(Commands.stopStreaming);
        setState(BTSrvState.DISCONNECTED); // Need this to handle the IOException
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
        Log.i(TAG, "Started dispatching...");
        int lostBytes = 0;
        try {
            int i = 0;
            int r = 0;
            byte[] pack = new byte[50];

            // Used continue to minimize the waste of bytes.
            // Used while true to use continue in a comfortable way.
            // Exit condition: (!dispatch || Thread.currentThread().isInterrupted()) with a break.
            while (true) {
                if (i > 0) {
                    lostBytes += i;     // The last cycle would have reset i if the packet had been accepted
                    Log.d(TAG, "Bytes lost: " + i + " total: " + lostBytes);
                    i = 0;
                }
                else if (r == SlipInputStream.EOS)
                    dispatchEOS();

                if (!dispatch) {
                    Log.d(TAG, "Dispatch thread end due to EOS or user action.");
                    break;
                }
                if (Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "Dispatch thread end due to a thread interruption.");
                    break;
                }

                // Start SLIP END (0 or the last)
                r = mInput.read();
                if (r != SlipInputStream.SLIP_END)
                    continue;
                // Start ASCII '9' (1) or SLIP END (0)
                r = mInput.read();
                if (r == SlipInputStream.SLIP_END)
                    r = mInput.read();
                // Start ASCII '9' (1)
                if (r != 0x39)
                    continue;

                i = mInput.read(pack, 0, 27 - 2);
                if (i < 27 - 2)
                    continue;

                if (pack[0] == (byte)0x02) {
                    i += mInput.read(pack, 0, 36 - (27 - 2) - 1);
                    if (i < 36 - 2 - 1)
                        continue;
                }

                // Last SLIP END
                r = mInput.read();
                if (mInput.read() == SlipInputStream.SLIP_END)
                    received(pack, 0, i);

                i = 0;
            }
        } catch (IOException e) {
            Log.i(TAG, "disconnection: " + e.getClass().getSimpleName());
            switch (e.getMessage()) {
                case "Operation Canceled":
                case "bt socket closed, read return: -1":
                    if (!dispatch) {
                        Log.d(TAG, "Legal BT socket disconnection");
                    } else {
                        Log.e(TAG, "Connection lost", e);
                        setState(BTSrvState.DISCONNECTED);
                        if (mStatusDelegate != null)
                            mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.CONNECTION_LOST);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        connect(100);
                    }
                    break;
                case "Software caused connection abort":
                    Log.e(TAG, "Software caused connection abort", e);
                    break;
                default:
                    Log.e(TAG, "Unmanaged disconnection", e);
                    break;
            }
        }
    }

    private void dispatchEOS() {
        dispatch = false;
        Log.e(TAG, "End of the stream reached.");
    }

    // Status

    public BTSrvState getState() {
        return mState;
    }

    protected void setState(BTSrvState state) {
        if (state == BTSrvState.DISCONNECTED)
            state = BTSrvState.DISCONNECTED;
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

    protected abstract void received(byte[] buffer, int offset, int bytes);

    // Subclasses

    public interface StatusDelegate {

        int
                READY = 0,
                CONNECTING = 1,
                CONNECTED = 2,
                DISCONNECTED = 3;

        void connecting(Wax9Receiver sender, BluetoothDevice device, boolean secureMode);
        void connected(Wax9Receiver sender, String deviceName);
        void disconnected(Wax9Receiver sender, DisconnectionCause cause);

        enum DisconnectionCause {
            DEVICE_NOT_FOUND,
            OTHER,
            IO_SOCKET_ERROR,
            CONNECTION_LOST,
            WRONG_PACKET_TYPE,
            DEVICE_NOT_SET,
            BLUETOOTH_IS_OFF,
            IO_STREAMS_ERROR
        }
    }

    public enum BTSrvState {
        IDLE,          // we're doing nothing
        CONNECTING,    // now initiating an outgoing connection
        CONNECTED,     // now connected to a remote device
        DISCONNECTED   // disconnected from device, error or
    }

    protected static class Commands {
        public static byte[] startStreaming = (
                                        // "rate x 0 0 100\r\n" +
                                        // "rate a 1 200 8\r\n" +
                                        // "rate g 1 200 2000\r\n" +
                                        // "rate m 1 80 0\r\n" +
                                        "datamode=1\r\n" +
                                        "stream\r\n").getBytes();
        public static byte[] stopStreaming = "reset\r\n".getBytes();
    }
}
