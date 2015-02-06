package eu.fbk.mpba.sensorsflows;

import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public interface OutputPlugIn<TimeT, ValueT> {

    // Abstracts to be implemented by the plug-in

    void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors);

    void outputPluginFinalize();

    void newSensorEvent(SensorEventEntry event);

    void newSensorData(SensorDataEntry<TimeT, ValueT> data);
}
