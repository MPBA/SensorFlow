package eu.fbk.mpba.sensorsflows;

import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public interface OutputPluginX<TimeT, ValueT> {

    void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors);

    void outputPluginFinalize();

    void newSensorEvent(SensorEventEntry<TimeT> event);

    void newSensorData(SensorDataEntry<TimeT, ValueT> data);
}
