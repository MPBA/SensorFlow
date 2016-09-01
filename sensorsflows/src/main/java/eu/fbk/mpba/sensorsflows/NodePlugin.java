package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.IPlugin;

public interface NodePlugin<TimeT, ValueT> extends IPlugin {

    void inputPluginStart();

    void inputPluginStop();

    Iterable<SensorComponent<TimeT, ValueT>> getSensors();
}
