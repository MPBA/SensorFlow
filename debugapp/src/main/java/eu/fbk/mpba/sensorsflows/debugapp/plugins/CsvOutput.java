package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugIn;
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
public class CsvOutput implements OutputPlugIn<Long, double[]> {

    String _name;
    String _path;
    CsvDataSaver _sav;
    List<List<Object>> headers = new ArrayList<>();
    List<ISensor> _linkedSensors = new ArrayList<>();

    public CsvOutput(String name, String path) {
        _name = name;
        _path = path;
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
        _sav = new CsvDataSaver(_path + "/" + sessionTag.toString() + "/",
                streamingSensors.toArray(), ".csv", ";", "\n");
        _linkedSensors.addAll(streamingSensors);
        for (ISensor l : streamingSensors) {
            List<Object> h = new ArrayList<>();
            h.add("timestamp");
            h.addAll(l.getValuesDescriptors());
            headers.add(h);
        }
        _sav.init(headers);
    }

    public void outputPluginFinalize() {

    }

    public void newSensorEvent(SensorEventEntry event) {

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
        return "CsvOutput-" + _name;
    }
}
