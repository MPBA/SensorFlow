package eu.fbk.mpba.sensorsflows;

import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISampleCounter;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public interface OutputPlugin<TimeT, ValueT> extends ISampleCounter {

    void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors);

    void outputPluginFinalize();

    void newSensorEvent(SensorEventEntry<TimeT> event);

    void newSensorData(SensorDataEntry<TimeT, ValueT> data);

    String getName();
}
