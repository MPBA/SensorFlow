package eu.fbk.mpba.sensorsflows.stubs;

import android.os.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;


public class CsvOutput extends OutputImpl<Long, float[]> {

    String _name;
    DataSaver _sav;
    List<List<Object>> headers = new ArrayList<List<Object>>();
    List<SensorImpl<Long, float[]>> _linkedSensors = new ArrayList<SensorImpl<Long, float[]>>();

    public CsvOutput(String name) {
        _name = name;
    }

    public List<String> getFiles() {
        List<String> a = new ArrayList<String>(_sav._files.length);
        for (int i = 0; i < _sav._files.length; i++) {
            a.add(_sav._files[i].getAbsolutePath());
        }
        return a;
    }

    @Override
    public void setLinkedSensors(List<SensorImpl> linkedSensors) {
        _sav = new DataSaver(Environment.getExternalStorageDirectory().getPath() +
                        "/eu.fbk.mpba.sensorsflows.stubs/" +
                        DataSaver.getHumanDateTimeName() + "/",
                        linkedSensors.toArray(), ".csv", ";", "\n");
        _linkedSensors.addAll((Collection)linkedSensors);
        for (SensorImpl l : linkedSensors) {
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
    protected void newSensorData(SensorDataEntry<Long, float[]> data) {
        List<Object> line = new ArrayList<Object>();
        line.add(data.time.toString());
        for (int i = 0; i < data.value.length; i++)
            line.add(data.value[i]);
        //noinspection SuspiciousMethodCalls
        _sav.writeCSV(_linkedSensors.indexOf(data.sensor), line);
    }

    @Override
    public String toString() {
        return "Output-" + _name;
    }
}
