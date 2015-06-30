package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for the device's data transport.
 *
 * @param <DeviceT> the desired type of device: it should be at least an IDevice.
 */
public interface IDeviceCallback<DeviceT extends IDevice> {
    // TODO (#is never used) check why it is not used
    void deviceStateChanged(DeviceT device, DeviceStatus state);
}
