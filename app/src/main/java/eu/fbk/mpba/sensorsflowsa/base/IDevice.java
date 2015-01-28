package eu.fbk.mpba.sensorsflowsa.base;

/**
 * Main interface for a device.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface IDevice<SensorT> {

    public void initialize();

    public Iterable<SensorT> getSensors();

    public DeviceStatus getState();

    public void finalizeDevice();
}
