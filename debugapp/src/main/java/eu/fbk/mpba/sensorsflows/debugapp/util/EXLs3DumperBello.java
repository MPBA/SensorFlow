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

                // In uno dei percorsi scarta bytes,
                // utilizzabile solo per controllare il packet counter
                try {
                    Log.i(TAG, "Streaming to file...");
                    // TODO 0 Test without
                    Thread.sleep(100);
                    int i;
                    byte[] pack = new byte[32];
                    while (!Thread.currentThread().isInterrupted()) {
                        i = 0;
                        pack[i++] = (byte) mStream.read();
                        /*  TODO 0 Test changing
                            t =
                                - 100Hz -> 10ms/p
                                - 200Hz ->  5ms/p
                                - 300Hz -> 10ms/3p = 3_333333ns/p > 3ms/p

                            Their impl: 5ms@100Hz -> t/2
                         */
                        Thread.sleep(3);
                        if (pack[0] == 0x20) {
                            pack[i++] = (byte) mStream.read();
                            if (pack[1] == EXLs3Manager.PacketType.AGMQB.id) {
                                while (i < 32)
                                    pack[i++] = (byte) mStream.read();
                            } else if (pack[1] == EXLs3Manager.PacketType.AGMB.id) {
                                while (i < 24)
                                    pack[i++] = (byte) mStream.read();
                            } else if (pack[1] == EXLs3Manager.PacketType.RAW.id
                                    || pack[1] == EXLs3Manager.PacketType.calib.id) {
                                while (i < 21)
                                    pack[i++] = (byte) mStream.read();
                            }
                            f.write(pack, 0, i);
                        }
                    }

                } catch (IOException e) {
                    Log.e(TAG, "+++ Disconnection: " + e.getMessage());
                    switch (e.getMessage()) {
                        case "":
                            break;
                        default:
                            Log.e(TAG, "Unmanageable disconnection", e);
                            break;
                    }
                } catch (InterruptedException e) {
                    Log.i(TAG, "Dispatch thread interrupted");
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