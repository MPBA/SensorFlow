package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class EXLs3Dumper {

    // Debug
    private static final String TAG = EXLs3Dumper.class.getSimpleName();

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
    protected Thread mDispatcher;

    public EXLs3Dumper(StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mStatusDelegate = statusDelegate;
        mDevice = device;
        mAdapter = adapter;
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
                    mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.IO_STREAMS_ERROR);
            }
        } else {
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
                throw new UnsupportedOperationException("Strange error", e);
            }
        } else
            startPending = true;
    }

    public void stopStream() {
        if (mOutput != null) {
            try {
                mOutput.write(new byte[] { 0x3A, 0x3A });
                f.flush();
                f.close();
            } catch (IOException ignored) { }
        } else
            startPending = false;
    }

    private int cnt = 0;
    private int counter_file = 0;
    private FileOutputStream f = null;

    public void dispatch() {

        try {
            File x = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/eu.fbk.mpba.sensorsflows/");

            x.mkdirs();

            f = new FileOutputStream(new File(x, "stream_" + CsvDataSaver.getHumanDateTimeString() + ".bin"));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.d(TAG, "+++++ Interrupted");
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] buffer = new byte[33];
                    buffer[cnt++] = (byte) mInput.read();
                    try {
                        Thread.sleep(3);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "+++++ Interrupted (in the loop)");
                    }
                    if (buffer[0] == 32) {
                        buffer[cnt++] = (byte) mInput.read();
//                      Log.d("NOW","1 - "+bytesToHex(buffer[0])+" / "+bytesToHex(buffer[1]));
                        if (buffer[0] == 32 && buffer[1] == -97) {
//                      Log.d("NOW","2");
                            while (cnt < 33) {
                                buffer[cnt++] = (byte) mInput.read();
                            }
//                      Log.d("NOW","3");

                            f.write(buffer);
//                            if(counter_file++>50) {
//                                f.flush();
//                                counter_file = 0;
//                            }
                        }
//                      Log.d("NOW","4");
                    }
                } catch (IOException e1) {
                    Log.d("Error", "Errore file = " + e1.getMessage());
                }
                cnt = 0;
            }

        } catch (IOException e)
        {
            Log.e(TAG, "Forced disconnection", e);
            // Connection Lost
            setState(BTSrvState.DISCONNECTED);
            if (mStatusDelegate != null)
                mStatusDelegate.disconnected(this, StatusDelegate.DisconnectionCause.CONNECTION_LOST);
        } finally

        {
            if (f != null)
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void close() {
        if (mDispatcher != null)
            if (mDispatcher.isAlive()) {
                mDispatcher.interrupt();
            }
        try {
            if (mInput != null)
                mInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (mOutput != null) {
                mOutput.flush();
                mOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (mSocket != null)
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
        } else
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
            socket = (BluetoothSocket) m.invoke(device, 1);
        } catch (NoSuchMethodException ignore) {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "IOException trying to create the socket", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to create the socket", e);
        }
        return socket;
    }

// Subclasses

    public static interface StatusDelegate {

        void connecting(EXLs3Dumper sender, BluetoothDevice device, boolean secureMode);

        void connected(EXLs3Dumper sender, String deviceName);

        void disconnected(EXLs3Dumper sender, DisconnectionCause cause);

        public enum DisconnectionCause {

            DEVICE_NOT_FOUND(8),
            IO_STREAMS_ERROR(16),
            IO_SOCKET_ERROR(24),
            CONNECTION_LOST(32),
            WRONG_PACKET_TYPE(40);

            public final int flag;

            DisconnectionCause(int v) {
                flag = v;
            }
        }
    }

    public enum BTSrvState {
        IDLE,          // we're doing nothing
        CONNECTING,    // now initiating an outgoing connection
        CONNECTED,     // now connected to a remote device
        DISCONNECTED   // disconnected from device, error or
    }
}