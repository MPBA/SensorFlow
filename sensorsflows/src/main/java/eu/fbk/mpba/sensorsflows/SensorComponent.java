package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.ISensorDataCallback;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorComponent<TimeT, ValueT> implements ISensor {
    protected DevicePlugin<TimeT, ValueT> _parent = null;
    protected ArrayList<OutputDecorator<TimeT, ValueT>> _outputs = new ArrayList<>();
    protected ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> _handler;

    protected boolean _listened = true;
    protected SensorStatus _status = SensorStatus.OFF;

    protected SensorComponent(DevicePlugin<TimeT, ValueT> parent) {
        _parent = parent;
    }

    void addOutput(OutputDecorator<TimeT, ValueT> _output) {
        _outputs.add(_output);
    }

    void setManager(ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT> man) {
        _handler = man;
    }

    Iterable<OutputDecorator<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<>(_outputs.iterator());
    }

    // Managed protected getters setters

    protected void changeStatus(SensorStatus state) {
        _handler.sensorStateChanged(this, null, _status = state);
    }

    // Managed Overrides

    @Override
    public SensorStatus getState() {
        return _status;
    }

    public DevicePlugin<TimeT, ValueT> getParentDevicePlugin() {
        return _parent;
    }

    // Notify methods
    public void sensorValue(TimeT time, ValueT value) {
        _handler.sensorValue(this, time, value);
    }

    public void sensorEvent(TimeT time, int type, String message) {
        _handler.sensorEvent(this, time, type, message);
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
