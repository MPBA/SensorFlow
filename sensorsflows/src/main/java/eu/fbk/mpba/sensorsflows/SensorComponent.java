package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorComponent<TimeT, ValueT> implements ISensor {
    private boolean _listened = true;
    private ArrayList<OutputDecorator<TimeT, ValueT>> _outputs = new ArrayList<>();
    private SensorStatus _status = SensorStatus.OFF;
    private DeviceDecorator<TimeT, ValueT> _parent = null;

    protected SensorComponent(DeviceDecorator<TimeT, ValueT> parent) {
        _parent = parent;
        ___manager = parent.getManager();
    }

    void addOutput(OutputDecorator<TimeT, ValueT> _output) {
        this._outputs.add(_output);
    }

    Iterable<OutputDecorator<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<>(_outputs.iterator());
    }

    // Managed protected getters setters

    protected void changeStatus(SensorStatus state) {
        _parent.getManager().sensorStateChanged(this, null, _status = state);
    }

    // Managed Overrides

    @Override
    public SensorStatus getState() {
        return _status;
    }

    @Override
    public DeviceDecorator<TimeT, ValueT> getParentDevice() {
        return _parent;
    }

    // Notify methods

    private FlowsMan<TimeT, ValueT> ___manager;
    public void sensorValue(TimeT time, ValueT value) {
        ___manager.sensorValue(this, time, value);
    }

    public void sensorEvent(TimeT time, int type, String message) {
        ___manager.sensorEvent(this, time, type, message);
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
