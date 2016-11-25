package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.INode;

/**
 * This class adds internal support for the library data-paths.
 */
class NodeDecorator implements INode<Flow> {
    private SensorFlow _manager = null;
    private DeviceStatus _status = DeviceStatus.NOT_INITIALIZED;
    private Input _input;

    NodeDecorator(Input input, SensorFlow manager) {
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
    public Iterable<Flow> getSensors() {
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

    SensorFlow getManager() {
        return _manager;
    }

    Input getPlugIn() {
        return _input;
    }
}
