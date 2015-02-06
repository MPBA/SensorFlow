package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorComponent<TimeT, ValueT> implements ISensor<DevicePlugIn<TimeT, ValueT>> {
    private boolean _listened = true;
    private ArrayList<OutputManager<TimeT, ValueT>> _outputs = new ArrayList<>();
    private SensorStatus _status = SensorStatus.OFF;
    private DevicePlugIn<TimeT, ValueT> _parent = null;

    protected SensorComponent(DevicePlugIn<TimeT, ValueT> parent) {
        _parent = parent;
    }

    void addOutput(OutputManager<TimeT, ValueT> _output) {
        this._outputs.add(_output);
    }

    Iterable<OutputManager<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<>(_outputs.iterator());
    }

    // Managed protected getters setters

    protected FlowsMan<TimeT, ValueT> getManager() {
        return getParentDevice()._manager;
    }

    protected void changeStatus(SensorStatus state) {
        _parent._manager.sensorStateChanged(this, null, _status = state);
    }

    /*protected void setParentDevice(DevicePlugIn<TimeT, ValueT> parent) {
        _parent = parent;
    }*/

    // Managed Overrides

    @Override
    public SensorStatus getState() {
        return _status;
    }

    @Override
    public DevicePlugIn<TimeT, ValueT> getParentDevice() {
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

    public abstract List<Object> getValuesDescriptors();

    @Override
    public abstract String toString();
}
