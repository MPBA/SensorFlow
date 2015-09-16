package eu.fbk.mpba.sensorsflows.base;

import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor extends ISampleCounter {

    void switchOnAsync();

    void switchOffAsync();

    SensorStatus getState();

    DevicePlugin getParentDevicePlugin();

    List<Object> getValueDescriptor();

    String getName();
}
