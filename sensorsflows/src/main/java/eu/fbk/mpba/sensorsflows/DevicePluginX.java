package eu.fbk.mpba.sensorsflows;

public interface DevicePluginX<TimeT, ValueT> {

    void inputPluginInitialize();

    void inputPluginFinalize();

    Iterable<SensorComponent<TimeT, ValueT>> getSensors();
}
