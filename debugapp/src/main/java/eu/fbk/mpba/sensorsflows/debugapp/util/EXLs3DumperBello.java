package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class EXLs3DumperBello {

    // Debug
    private static final String TAG = EXLs3DumperBello.class.getSimpleName();

    // UUID for rfcomm connection
    @SuppressWarnings("SpellCheckingInspection")
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    protected Thread mDispatcher;
    protected EXLs3Stream mStream;

    public EXLs3DumperBello(EXLs3Stream.StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        mStream = new EXLs3Stream(statusDelegate, device, adapter);
        mDispatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                dispatch();
            }
        });
    }

    // Operation

    public void connect() {
        mStream.connect();
        if (mStream.getState() == EXLs3Stream.BTSrvState.CONNECTED) {
            mDispatcher = new Thread(new Runnable() {
                @Override
                public void run() {
                    dispatch();
                }
            });
            mDispatcher.start();
        }
        else
            close();
    }

    public void startStream() {
        mStream.startStream();
    }

    public void stopStream() {
        mStream.stopStream();
    }

    public void dispatch() {
        File x = new File(Environment.getExternalStorageDirectory().getPath()
                + "/eu.fbk.mpba.sensorsflows/");

        if (!x.mkdirs() && !x.isDirectory()) {
            Log.e(TAG, "MKDIRS FALSE for " + x.getAbsolutePath());
            close();
        }
        else {
            FileOutputStream f;
            try {
                f = new FileOutputStream(new File(x, "stream_" + CsvDataSaver.getHumanDateTimeString() + ".bin"));

                try {
                    Log.i(TAG, "Streaming to file...");
                    Thread.sleep(100);
                    while (!Thread.currentThread().isInterrupted()) {
                        f.write(mStream.read());
                        Thread.sleep(3);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Forced disconnection", e);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted");
                } finally {
                    try {
                        f.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //noinspection ThrowFromFinallyBlock
                        throw new UnknownError("so8ov794us");
                    }
                }
            } catch (FileNotFoundException e) {
                Log.wtf(TAG, "UnknownError", e);
            }
        }
        Log.d(TAG, "Dispatch thread end");
    }

    public void close() {
        mStream.close();
        mDispatcher.interrupt();
    }

    // Status

    public EXLs3Stream.BTSrvState getState() {
        return mStream.getState();
    }

}