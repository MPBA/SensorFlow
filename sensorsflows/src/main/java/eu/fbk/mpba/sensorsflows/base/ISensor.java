package eu.fbk.mpba.sensorsflows.base;

import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor {

    void switchOnAsync();

    void switchOffAsync();

    SensorStatus getStatus();

    NodePlugin getParentDevicePlugin();

    List<Object> getValueDescriptor();

    String getName();

    IMonoTimestampSource getTime();
}
