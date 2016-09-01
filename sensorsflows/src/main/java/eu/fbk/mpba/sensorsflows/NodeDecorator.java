package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.INode;

/**
 * This class adds internal support for the library data-paths.
 */
class NodeDecorator<TimeT, ValueT> implements INode<SensorComponent<TimeT, ValueT>> {
    private FlowsMan<TimeT, ValueT> _manager = null;
    private DeviceStatus _status = DeviceStatus.NOT_INITIALIZED;
    private NodePlugin<TimeT, ValueT> _nodePlugin;

    NodeDecorator(NodePlugin<TimeT, ValueT> nodePlugin, FlowsMan<TimeT, ValueT> manager) {
        _nodePlugin = nodePlugin;
        _manager = manager;
    }

    protected void changeStatus(DeviceStatus s) {
        if (_manager != null)
            _manager.deviceStatusChanged(this, _status = s);
    }

    @Override
    public void initializeNode() {
        changeStatus(DeviceStatus.INITIALIZING);
        _nodePlugin.inputPluginStart();
        changeStatus(DeviceStatus.INITIALIZED);
    }

    @Override
    public Iterable<SensorComponent<TimeT, ValueT>> getSensors() {
        return _nodePlugin.getSensors();
    }

    @Override
    public void finalizeNode() {
        changeStatus(DeviceStatus.FINALIZING);
        _nodePlugin.inputPluginStop();
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

    NodePlugin<TimeT, ValueT> getPlugIn() {
        return _nodePlugin;
    }
}
