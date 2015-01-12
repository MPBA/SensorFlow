package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorImpl<TimeT, ValueT> implements ISensor<DeviceImpl<TimeT, ValueT>> {
    private boolean _listened = true;
    DeviceImpl<TimeT, ValueT> _parent = null;
    SensorStatus _status = SensorStatus.OFF;

    protected void changeStatus(SensorStatus state) {
        _parent._manager.sensorStateChanged(this, null, _status = state);
    }

    @Override
    public SensorStatus getState() {
        return _status;
    }

    protected void setParentDevice(DeviceImpl<TimeT, ValueT> parent) {
        _parent = parent;
    }

    @Override
    public DeviceImpl<TimeT, ValueT> getParentDevice() {
        return _parent;
    }

    public boolean isListened() {
        return _listened;
    }

    public void setListened(boolean listened) {
        this._listened = listened;
    }

    private ArrayList<OutputImpl<TimeT, ValueT>> _outputs = new ArrayList<OutputImpl<TimeT, ValueT>>();

    void addOutput(OutputImpl<TimeT, ValueT> _output) {
        this._outputs.add(_output);
    }

    Iterable<OutputImpl<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<OutputImpl<TimeT, ValueT>>(_outputs.iterator());
    }

    protected FlowsMan<TimeT, ValueT> getManager() {
        return getParentDevice()._manager;
    }

    public void sensorValue(TimeT time, ValueT value) {
        getManager().sensorValue(this, time, value);
    }

    public void sensorEvent(TimeT time, int type, String message) {
        getManager().sensorEvent(this, time, type, message);
    }
}
