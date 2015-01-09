package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.Iterator;

import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorImpl<TimeT, ValueT> implements ISensor<DeviceImpl> {
    private boolean _listened = false;

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

}
