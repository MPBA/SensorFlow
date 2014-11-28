package eu.fbk.mpba.sensorsflows.base;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor {

    public void switchOnAsync();

    public void switchOffAsync();

    public SensorStatus getState();

    public IDevice getParentDevice();
}
