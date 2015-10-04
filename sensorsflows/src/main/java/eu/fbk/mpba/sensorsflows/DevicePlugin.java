package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.IStandard;

public interface DevicePlugin<TimeT, ValueT> extends IStandard {

    void inputPluginInitialize();

    void inputPluginFinalize();

    Iterable<SensorComponent<TimeT, ValueT>> getSensors();
}
