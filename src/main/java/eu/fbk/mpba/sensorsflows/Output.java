package eu.fbk.mpba.sensorsflows;

import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.IPlugin;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public interface Output<TimeT, ValueT> extends IPlugin {

    void onOutputStart(Object sessionTag, List<ISensor> streamingSensors);

    void onOutputStop();

    void onEvent(SensorEventEntry<TimeT> event);

    void onValue(SensorDataEntry<TimeT, ValueT> data);
}
