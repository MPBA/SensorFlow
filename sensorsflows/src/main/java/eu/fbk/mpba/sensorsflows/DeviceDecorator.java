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

    protected void changeState(DeviceStatus s) {
        if (_manager != null)
            _manager.deviceStateChanged(this, _status = s);
    }

    @Override
    public void initializeDevice() {
        changeState(DeviceStatus.INITIALIZING);
        _devicePlugin.inputPluginInitialize();
        changeState(DeviceStatus.INITIALIZED);
    }

    @Override
    public Iterable<SensorComponent<TimeT, ValueT>> getSensors() {
        return _devicePlugin.getSensors();
    }

    @Override
    public void finalizeDevice() {
        changeState(DeviceStatus.FINALIZING);
        _devicePlugin.inputPluginFinalize();
        changeState(DeviceStatus.FINALIZED);
    }

    // Getters

    @Override
    public DeviceStatus getState() {
        return _status;
    }

    FlowsMan<TimeT, ValueT> getManager() {
        return _manager;
    }

    DevicePlugin<TimeT, ValueT> getPlugIn() {
        return _devicePlugin;
    }
}
