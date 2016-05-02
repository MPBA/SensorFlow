package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.IDevice;

/**
 * This class adds internal support for the library data-paths.
 */
class DeviceDecorator<TimeT, ValueT> implements IDevice<SensorComponent<TimeT, ValueT>> {
    private FlowsMan<TimeT, ValueT> _manager = null;
    private DeviceStatus _status = DeviceStatus.NOT_INITIALIZED;
    private DevicePlugin<TimeT, ValueT> _devicePlugin;

    DeviceDecorator(DevicePlugin<TimeT, ValueT> devicePlugin, FlowsMan<TimeT, ValueT> manager) {
        _devicePlugin = devicePlugin;
        _manager = manager;
    }

    protected void changeStatus(DeviceStatus s) {
        if (_manager != null)
            _manager.deviceStatusChanged(this, _status = s);
    }

    @Override
    public void initializeDevice() {
        changeStatus(DeviceStatus.INITIALIZING);
        _devicePlugin.inputPluginInitialize();
        changeStatus(DeviceStatus.INITIALIZED);
    }

    @Override
    public Iterable<SensorComponent<TimeT, ValueT>> getSensors() {
        return _devicePlugin.getSensors();
    }

    @Override
    public void finalizeDevice() {
        changeStatus(DeviceStatus.FINALIZING);
        _devicePlugin.inputPluginFinalize();
        changeStatus(DeviceStatus.FINALIZED);
    }

    // Getters

    @Override
    public DeviceStatus getStatus() {
        return _status;
    }

    FlowsMan<TimeT, ValueT> getManager() {
        return _manager;
    }

    DevicePlugin<TimeT, ValueT> getPlugIn() {
        return _devicePlugin;
    }
}
