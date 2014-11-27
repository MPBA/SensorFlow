package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for the device's data transport.
 *
 * @param <DeviceT> the desired type of device: it should be at least an IDevice.
 */
public interface IDeviceCallback<DeviceT> {

    void deviceEvent(DeviceT device, int type, String message);

    void deviceStateChanged(DeviceT device, DeviceStatus state);
}
