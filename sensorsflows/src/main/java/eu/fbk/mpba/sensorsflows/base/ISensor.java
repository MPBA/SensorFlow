package eu.fbk.mpba.sensorsflows.base;

import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor extends ISampleCounter {

    public void switchOnAsync();

    public void switchOffAsync();

    public SensorStatus getState();

    public DevicePlugin getParentDevicePlugin();

    public abstract List<Object> getValuesDescriptors();

    public abstract String getName();
}
