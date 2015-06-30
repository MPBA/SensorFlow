package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.ISampleCounter;

public interface DevicePlugin<TimeT, ValueT> {

    void inputPluginInitialize();

    void inputPluginFinalize();

    Iterable<SensorComponent<TimeT, ValueT>> getSensors();
}
