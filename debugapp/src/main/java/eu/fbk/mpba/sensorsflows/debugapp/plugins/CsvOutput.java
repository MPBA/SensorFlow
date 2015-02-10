package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.debugapp.util.CsvDataSaver;

/**
 * Comma Separated Values Output Plug-In
 *
 * This plug-in saves the data in a CSV file. The table is composed by the timestamp column and a
 * column for each float value in the array (the ValueT type is specified).
 */
public class CsvOutput implements OutputPlugin<Long, double[]> {

    private final String _tsCol;
    private final String _ext;
    private final String _nl;
    private final String _sep;
    String _name;
    String _path;
    CsvDataSaver _sav;
    List<List<Object>> headers = new ArrayList<>();
    List<ISensor> _linkedSensors = new ArrayList<>();

    public CsvOutput(String name, String path) {
        this(name, path, "timestamp", ".csv", ";", "\n");
    }

    public CsvOutput(String name, String path, String timestampColumnName, String fileSuffix, String separator, String newLine) {
        _name = name;
        _path = path;
        _tsCol = timestampColumnName;
        _ext = fileSuffix;
        _sep = separator;
        _nl = newLine;
    }

    public List<String> getFiles() {
        List<File> f = _sav.getSupports();
        List<String> a = new ArrayList<>(f.size());
        for (int i = 0; i < f.size(); i++) {
            a.add(f.get(i).getAbsolutePath());
        }
        return a;
    }

    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        _sav = new CsvDataSaver(_path + "/" + sessionTag.toString() + "/" + toString(),
                streamingSensors.toArray(), _ext, _sep, _nl);
        _linkedSensors.addAll(streamingSensors);
        for (ISensor l : streamingSensors) {
            List<Object> h = new ArrayList<>();
            h.add(_tsCol);
            h.addAll(l.getValuesDescriptors());
            headers.add(h);
        }
        _sav.init(headers);
    }

    public void outputPluginFinalize() {
        _sav.close();
    }

    public void newSensorEvent(SensorEventEntry event) {
        // TODO Manage events
    }

    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        List<Object> line = new ArrayList<>();
        line.add(data.time.toString());
        for (int i = 0; i < data.value.length; i++)
            line.add(data.value[i]);
        //noinspection SuspiciousMethodCalls
        _sav.save(_linkedSensors.indexOf(data.sensor), line);
    }

    @Override
    public String toString() {
        return CsvOutput.class.getSimpleName() + "-" + _name;
    }
}
