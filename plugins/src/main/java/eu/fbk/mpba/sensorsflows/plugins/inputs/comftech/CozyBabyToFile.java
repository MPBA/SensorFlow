package eu.fbk.mpba.sensorsflows.plugins.inputs.comftech;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.fbk.mpba.sensorsflows.plugins.outputs.CsvDataSaver;

public class CozyBabyToFile extends CozyBabyReceiver {

    // Debug
    private static final String TAG = CozyBabyToFile.class.getSimpleName();

    // Member fields
    private FileOutputStream mOut;

    public CozyBabyToFile(StatusDelegate statusDelegate, BluetoothDevice device, BluetoothAdapter adapter) {
        super(statusDelegate, device, adapter, false);
    }

    // Operation

    @SuppressWarnings("SpellCheckingInspection")
    public void start() {
        connect(3);
        if (getState() == BTSrvState.CONNECTED) {
            File x = new File(Environment.getExternalStorageDirectory().getPath()
                    + "/eu.fbk.mpba.sensorsflows.plugins.inputs.comftech/");
            if (x.mkdirs() || x.isDirectory())
                try {
                    mOut = new FileOutputStream(new File(x, "cozybaby_" + CsvDataSaver.getHumanDateTimeString() + ".bin"));
                    command(Commands.startStreaming);
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

    public void received(byte[] buffer, int offset, int bytes) {
        try {
            mOut.write(buffer, offset, bytes);
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "+++ On write to file", e);
        }
    }
}
