package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.INode;

/**
 * This class adds internal support for the library data-paths.
 */
class NodeDecorator<TimeT, ValueT> implements INode<Flow<TimeT, ValueT>> {
    private SensorFlow<TimeT, ValueT> _manager = null;
    private DeviceStatus _status = DeviceStatus.NOT_INITIALIZED;
    private Input<TimeT, ValueT> _input;

    NodeDecorator(Input<TimeT, ValueT> input, SensorFlow<TimeT, ValueT> manager) {
        _input = input;
        _manager = manager;
    }

    protected void changeStatus(DeviceStatus s) {
        if (_manager != null)
            _manager.deviceStatusChanged(this, _status = s);
    }

    @Override
    public void initializeNode() {
        changeStatus(DeviceStatus.INITIALIZING);
        _input.onInputStart();
        changeStatus(DeviceStatus.INITIALIZED);
    }

    @Override
    public Iterable<Flow<TimeT, ValueT>> getSensors() {
        return _input.getFlows();
    }

    @Override
    public void finalizeNode() {
        changeStatus(DeviceStatus.FINALIZING);
        _input.onInputStop();
        changeStatus(DeviceStatus.FINALIZED);
    }

    // Getters

    @Override
    public DeviceStatus getStatus() {
        return _status;
    }

    SensorFlow<TimeT, ValueT> getManager() {
        return _manager;
    }

    Input<TimeT, ValueT> getPlugIn() {
        return _input;
    }
}
