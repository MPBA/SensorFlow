package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.INamed;

public interface DevicePlugin<TimeT, ValueT> extends INamed {

    void inputPluginInitialize();

    void inputPluginFinalize();

    Iterable<SensorComponent<TimeT, ValueT>> getSensors();

    void close();
}
