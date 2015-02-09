package eu.fbk.mpba.sensorsflows;

public interface DevicePlugin<TimeT, ValueT> {

    void inputPluginInitialize();

    void inputPluginFinalize();

    Iterable<SensorComponent<TimeT, ValueT>> getSensors();
}
