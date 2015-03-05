package eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    CsvDataSaver _savData;
    CsvDataSaver _savEvents;
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
        List<File>
        f = _savData.getSupports();
        f.addAll(_savEvents.getSupports());
        List<String> a = new ArrayList<>(f.size());
        for (int i = 0; i < f.size(); i++) {
            a.add(f.get(i).getAbsolutePath());
        }
        return a;
    }

    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        _savData = new CsvDataSaver(_path + "/" + sessionTag.toString() + "/data_" + toString(),
                streamingSensors.toArray(), _ext, _sep, _nl);
        _savEvents = new CsvDataSaver(_path + "/" + sessionTag.toString() + "/events_" + toString(),
                streamingSensors.toArray(), _ext, _sep, _nl);
        _linkedSensors.addAll(streamingSensors);
        List<List<Object>> dataH = new ArrayList<>();
        List<List<Object>> evtH = new ArrayList<>();
        for (ISensor l : streamingSensors) {
            List<Object> h = new ArrayList<>();
            h.add(_tsCol);
            h.addAll(l.getValuesDescriptors());
            dataH.add(h);
            evtH.add(Arrays.asList((Object)_tsCol, "code", "message"));
        }
        _savData.init(dataH);
        _savEvents.init(evtH);
    }

    public void outputPluginFinalize() {
        _savData.close();
        _savEvents.close();
    }

    public void newSensorEvent(SensorEventEntry event) {
        List<Object> line = new ArrayList<>();
        line.add(event.timestamp.toString());
        line.add(event.code);
        line.add(event.message);
        _savEvents.save(_linkedSensors.indexOf(event.sensor), line);
    }

    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        List<Object> line = new ArrayList<>();
        line.add(data.timestamp.toString());
        for (int i = 0; i < data.value.length; i++)
            line.add(data.value[i]);
        _savData.save(_linkedSensors.indexOf(data.sensor), line);
    }

    @Override
    public String toString() {
        return CsvOutput.class.getSimpleName() + "-" + _name;
    }
}
