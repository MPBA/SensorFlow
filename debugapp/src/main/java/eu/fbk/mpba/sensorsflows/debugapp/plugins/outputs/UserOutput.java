package eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs;

import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.debugapp.plugins.EXLs3Device;
import eu.fbk.mpba.sensorsflows.debugapp.util.EXLs3Manager;

public class UserOutput implements OutputPlugin<Long, double[]> {

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {

    }

    @Override
    public void outputPluginFinalize() {

    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        if (event.sensor instanceof EXLs3Device.EXLSensor
            && (event.code & EXLs3Manager.StatusDelegate.DISCONNECTED) != 0) {
            ((EXLs3Device.EXLSensor)event.sensor).connect();
        }
    }

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {

    }
}
