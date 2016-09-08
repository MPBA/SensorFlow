package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for a device.
 *
 * The user should access to these methods only to have a higher control of the operation.
 */
public interface INode<SensorT> {

    void initializeNode();

    Iterable<SensorT> getSensors();

    DeviceStatus getStatus();

    void finalizeNode();
}
