package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.IDevice;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class DeviceImpl<TimeT, ValueT> implements IDevice<SensorImpl> {
    FlowsMan<TimeT, ValueT> _manager = null;
    private DeviceStatus _status = DeviceStatus.NOT_INITIALIZED;

    void setOutputCallbackManager(FlowsMan<TimeT, ValueT> manager) {
        _manager = manager;
    }

    protected void changeState(DeviceStatus s) {
        if (_manager != null)
            _manager.deviceStateChanged(this, _status = s);
    }

    @Override
    public void initialize() {
        changeState(DeviceStatus.INITIALIZING);
        pluginInitialize();
        changeState(DeviceStatus.INITIALIZED);
    }

    @Override
    public void finalizeDevice() {
        changeState(DeviceStatus.FINALIZING);
        pluginFinalize();
        changeState(DeviceStatus.FINALIZED);
    }

    // Getters

    @Override
    public DeviceStatus getState() {
        return _status;
    }

    // Abstracts to be implemented by the plug-in

    protected abstract void pluginInitialize();

    protected abstract void pluginFinalize();
}
