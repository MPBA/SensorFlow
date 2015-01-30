package eu.fbk.mpba.sensorsflows.gpspluginapp.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugIn;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.gpspluginapp.util.DataSaver;

/**
 * Comma Separated Values Output Plug-In
 *
 * This plug-in saves the data in a CSV file. The table is composed by the timestamp column and a
 * column for each float value in the array (the ValueT type is specified).
 */
public class CsvOutput extends OutputPlugIn<Long, double[]> {

    String _name;
    String _path;
    DataSaver _sav;
    List<List<Object>> headers = new ArrayList<List<Object>>();
    List<SensorComponent<Long, double[]>> _linkedSensors = new ArrayList<SensorComponent<Long, double[]>>();

    public CsvOutput(String name, String path) {
        _name = name;
        _path = path;
    }

    public List<String> getFiles() {
        List<File> f = _sav.getFiles();
        List<String> a = new ArrayList<>(f.size());
        for (int i = 0; i < f.size(); i++) {
            a.add(f.get(i).getAbsolutePath());
        }
        return a;
    }

    @Override
    public void setLinkedSensors(List<SensorComponent> linkedSensors) {
        _sav = new DataSaver(_path +
                        DataSaver.getHumanDateTimeName() + "/",
                        linkedSensors.toArray(), ".csv", ";", "\n");
        _linkedSensors.addAll((Collection)linkedSensors);
        for (SensorComponent l : linkedSensors) {
            List<Object> h = new ArrayList<Object>();
            h.add("timestamp");
            h.addAll(l.getValuesDescriptors());
            headers.add(h);
        }
    }

    @Override
    protected void pluginInitialize() {
        _sav.initFS(headers);
    }

    @Override
    protected void pluginFinalize() {

    }

    @Override
    protected void newSensorEvent(SensorEventEntry event) {

    }

    @Override
    protected void newSensorData(SensorDataEntry<Long, double[]> data) {
        List<Object> line = new ArrayList<Object>();
        line.add(data.time.toString());
        for (int i = 0; i < data.value.length; i++)
            line.add(data.value[i]);
        //noinspection SuspiciousMethodCalls
        _sav.writeCSV(_linkedSensors.indexOf(data.sensor), line);
    }

    @Override
    public String toString() {
        return "CsvOutput-" + _name;
    }
}
