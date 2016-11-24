package eu.fbk.mpba.sensorsflows.base;

import java.util.List;

import eu.fbk.mpba.sensorsflows.Input;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor {

    void switchOnAsync();

    void switchOffAsync();

    SensorStatus getStatus();

    Input getParentInput();

    List<Object> getHeader();

    String getName();
}
