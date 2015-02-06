package eu.fbk.mpba.sensorsflows.debugapp.util;

import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugIn;
import eu.fbk.mpba.sensorsflows.OutputPlugIn;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public class SplitterSQLite extends DevicePlugIn<Long,double[]> implements OutputPlugIn<Long,double[]> {

    @Override
    protected void inputPluginInitialize() {

    }

    @Override
    protected void inputPluginFinalize() {

    }

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {

    }

    @Override
    public void outputPluginFinalize() {

    }

    @Override
    public void newSensorEvent(SensorEventEntry event) {

    }

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {

    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return null;
    }
}
