package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for a device.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface IDevice<SensorT> {

    void initializeDevice();

    Iterable<SensorT> getSensors();

    DeviceStatus getState();

    void finalizeDevice();
}
