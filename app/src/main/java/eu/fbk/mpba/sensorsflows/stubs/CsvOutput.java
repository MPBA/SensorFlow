package eu.fbk.mpba.sensorsflows.stubs;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;


public class CsvOutput extends OutputImpl<Long, float[]> {

    String _name;
    DataSaver _sav;
    List<String[]> headers = new ArrayList<String[]>();

    public CsvOutput() {
        _name = "CsvOutput";
    }

    @Override
    public void setLinkedSensors(List<SensorImpl> linkedSensors) {
        _sav = new DataSaver("/storage/sdcard0/eu.fbk.mpba.sensorsflows.stubs/" +
                        DataSaver.getHumanDateTimeName() + "/",
                        linkedSensors.toArray(), ".csv", ";", "\n");
        for (SensorImpl l : linkedSensors) {
            headers.add((String[]) (l.getValuesDescriptors().toArray()));
        }
    }

    @Override
    protected void pluginInitialize() {
        _sav.initFS((String[][])(headers.toArray()));
    }

    @Override
    protected void pluginFinalize() {

    }

    @Override
    protected void newSensorEvent(SensorEventEntry event) {
    }

    @Override
    protected void newSensorData(SensorDataEntry<Long, float[]> data) {

    }

    @Override
    public String toString() {
        return "Output:" + _name;
    }
}
