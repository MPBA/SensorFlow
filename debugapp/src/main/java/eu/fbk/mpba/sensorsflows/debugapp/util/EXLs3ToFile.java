package eu.fbk.mpba.sensorsflows.debugapp.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EXLs3ToFile extends EXLs3Receiver {

    // Debug
    private static final String TAG = EXLs3ToFile.class.getSimpleName();

    // Member fields
    private FileOutputStream mOut;

    public EXLs3ToFile(EXLs3Receiver.StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        super(statusDelegate, device, adapter);
    }

    // Operation

    @SuppressWarnings("SpellCheckingInspection")
    public void start() {
        connect();
        if (getState() == EXLs3Receiver.BTSrvState.CONNECTED) {
            File x = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/eu.fbk.mpba.sensorsflows/");
            if (x.mkdirs() || x.isDirectory())
                try {
                    mOut = new FileOutputStream(new File(x, "stream_" + CsvDataSaver.getHumanDateTimeString() + ".bin"));
                    startStream();
                    return;
                } catch (FileNotFoundException e) {
                    //noinspection SpellCheckingInspection
                    Log.wtf("Perché?", e);
                }
            Log.e(TAG, "File system error for " + x.getAbsolutePath());
            close();
        }
    }

    public void stop() {
        close();
        try {
            if (mOut != null) {
                mOut.flush();
                mOut.close();
            }
        } catch (IOException  e) {
            Log.e(TAG, "+++ On close file", e);
        }
    }

    public void received(byte[] buffer, int bytes) {
        try {
            mOut.write(buffer, 0, bytes);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "+++ On write to file", e);
        }
    }
}
