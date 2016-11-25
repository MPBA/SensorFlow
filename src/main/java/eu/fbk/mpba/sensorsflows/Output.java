package eu.fbk.mpba.sensorsflows;

import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.IPlugin;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public interface Output extends IPlugin {

    void onOutputStart(Object sessionTag, List<ISensor> streamingSensors);

    void onOutputStop();

    void onEvent(SensorEventEntry event);

    void onValue(SensorDataEntry data);
}
