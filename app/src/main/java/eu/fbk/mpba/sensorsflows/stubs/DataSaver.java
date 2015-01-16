package eu.fbk.mpba.sensorsflows.stubs;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DataSaver {
    String[] _paths;
    FileWriter[] _writs;
    int[] _csvCard;
    String _sep;
    String _nl;

    public DataSaver(String prefix, Object[] names, String suffix, String sep, String nl) {
        _paths = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            _paths[i] = prefix + names[i] + suffix;
        }
        _writs = new FileWriter[names.length];
        _csvCard = new int[names.length];
        _sep = sep;
        _nl = nl;
    }

    public static String getHumanDateTimeName() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.ITALY).format(new Date());
    }

    public static File getBasePath(String baseFolder) {
        return new File(Environment.getExternalStorageDirectory() + "/" + baseFolder);
    }

    public static String buildBasePath(String baseFolder) {
        File storagePath = new File(Environment.getExternalStorageDirectory() + "/" + baseFolder);
        Log.d("ALE DATA", "Base path = " + storagePath.getAbsolutePath());
        if (!storagePath.exists() && !storagePath.mkdirs())
            Log.e("ALE DATA", "Path non creabile " + storagePath.getAbsolutePath());
        return storagePath.getAbsolutePath() + "/";
    }

    public boolean initFS(String[][] headers) {
        for (int i = 0; i < _paths.length; i++)
            try {
                File f = new File(_paths[i]);
                Log.d("ALE DATA", "Creating " + _paths[i] + " mkdirs:" + f.getParentFile().mkdirs());
                boolean header = !f.exists();
                _writs[i] = new FileWriter(f, true);
                _csvCard[i] = headers[i].length;
                if (header)
                    writeCSV(i, headers[i]);
            } catch (IOException e) {
                e.printStackTrace();
                for (int j = 0; j < i; j++)
                    if (_writs[j] != null)
                        try {
                            _writs[j].close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                return false;
            }
        return true;
    }

    public void close() {
        for (int i = 0; i < _paths.length; i++)
            try {
                _writs[i].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void writeCSV(int file, Object[] data) throws IllegalArgumentException {
        int length = data.length;
        while (length > _csvCard[file] && data[length - 1] == null)
            length--;
        if (length == _csvCard[file])
            try {
                _writs[file].write(data[0].toString());
                for (int i = 1; i < length; i++) {
                    _writs[file].write(_sep);
                    _writs[file].write(data[i].toString());
                }
                _writs[file].write(_nl);
                _writs[file].flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        else
            throw new IllegalArgumentException(
                    "Wrong number of columns given for file n." + file + "!");
    }
}
