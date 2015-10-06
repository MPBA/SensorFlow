package eu.fbk.mpba.sensorsflows.plugins.outputs;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Simple utility class to write multiple csv files.
 */
public class CsvDataSaver {
    String[] _paths;
    FileWriter[] _writs;
    File[] _files;
    int[] _csvCard;
    String _sep;
    String _nl;

    public CsvDataSaver(String prefix, Object[] names, String suffix, String sep, String nl) {
        _paths = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            _paths[i] = prefix + names[i] + suffix;
        }
        _writs = new FileWriter[names.length];
        _files = new File[names.length];
        _csvCard = new int[names.length];
        _sep = sep;
        _nl = nl;
    }

    public static String getHumanDateTimeString() {
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

    public boolean init(List<List<Object>> headers) {
        for (int i = 0; i < _paths.length; i++)
            try {
                _files[i] = new File(_paths[i]);
                //noinspection ResultOfMethodCallIgnored
                _files[i].getParentFile().mkdirs();
                boolean header = !_files[i].exists();
                _writs[i] = new FileWriter(_files[i], true);
                _csvCard[i] = headers.get(i).size();
                if (header)
                    save(i, headers.get(i));
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

    public void save(int file, List<Object> data) throws IllegalArgumentException {
        int length = data.size();
        while (length > _csvCard[file] && data.get(length - 1) == null)
            length--;
        if (length == _csvCard[file])
            try {
                _writs[file].write(data.get(0).toString());
                for (int i = 1; i < length; i++) {
                    _writs[file].write(_sep);
                    _writs[file].write(data.get(i).toString());
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

    public List<File> getSupports() {
        return Arrays.asList(_files);
    }
}
