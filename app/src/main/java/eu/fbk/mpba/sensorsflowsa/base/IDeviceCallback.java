package eu.fbk.mpba.sensorsflowsa.base;

/**
 * Main interface for the device's data transport.
 *
 * @param <DeviceT> the desired type of device: it should be at least an IDevice.
 */
public interface IDeviceCallback<DeviceT extends IDevice> {
    void deviceStateChanged(DeviceT device, DeviceStatus state);
}
