package eu.fbk.mpba.sensorsflowsa.base;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor<DeviceT extends IDevice> {

    public void switchOnAsync();

    public void switchOffAsync();

    public SensorStatus getState();

    public DeviceT getParentDevice();
}
