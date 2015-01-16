package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorImpl<TimeT, ValueT> implements ISensor<DeviceImpl<TimeT, ValueT>> {
    private boolean _listened = true;
    private ArrayList<OutputImpl<TimeT, ValueT>> _outputs = new ArrayList<OutputImpl<TimeT, ValueT>>();
    DeviceImpl<TimeT, ValueT> _parent = null;
    SensorStatus _status = SensorStatus.OFF;

    void addOutput(OutputImpl<TimeT, ValueT> _output) {
        this._outputs.add(_output);
    }

    Iterable<OutputImpl<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<OutputImpl<TimeT, ValueT>>(_outputs.iterator());
    }

    int getOutputsCount() {
        return _outputs.size();
    }

    DeviceImpl<TimeT, ValueT> _parent = null;
    // Managed protected getters setters

    protected FlowsMan<TimeT, ValueT> getManager() {
        return getParentDevice()._manager;
    }

    protected void changeStatus(SensorStatus state) {
        _parent._manager.sensorStateChanged(this, null, _status = state);
    }

    protected void setParentDevice(DeviceImpl<TimeT, ValueT> parent) {
        _parent = parent;
    }

    // Managed Overrides

    @Override
    public SensorStatus getState() {
        return _status;
    }

    @Override
    public DeviceImpl<TimeT, ValueT> getParentDevice() {
        return _parent;
    }

    // Notify methods

    public void sensorValue(TimeT time, ValueT value) {
        getManager().sensorValue(this, time, value);
    }

    public void sensorEvent(TimeT time, int type, String message) {
        getManager().sensorEvent(this, time, type, message);
    }

    // Listenage

    public boolean isListened() {
        return _listened;
    }

    public void setListened(boolean listened) {
        this._listened = listened;
    }

    // To implement

    public abstract List<String> getValuesDescriptors();
}
