package eu.fbk.mpba.sensorsflows.base;

import java.util.List;

/**
 * Main control interface for a sensor.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface ISensor {

    public void switchOnAsync();

    public void switchOffAsync();

    public SensorStatus getState();

    public Object getParentDevice();

    public abstract List<Object> getValuesDescriptors();
}
