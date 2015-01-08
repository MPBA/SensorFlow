package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.Enumeration;

import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.util.IterToEnum;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorImpl<TimeT, ValueT> implements ISensor<DeviceImpl> {
    private int _avgStreamingInterval = 0;


    private boolean _listened = false;

    public boolean isListened() {
        return _listened;
    }

    public void setListened(boolean listened) {
        this._listened = listened;
    }


    private ArrayList<IOutput<TimeT, ValueT>> _outputs = new ArrayList<IOutput<TimeT, ValueT>>();

    void addOutput(IOutput<TimeT, ValueT> _output) {
        this._outputs.add(_output);
    }

    Enumeration<IOutput<TimeT, ValueT>> getOutputs() {
        return new IterToEnum<IOutput<TimeT, ValueT>>(_outputs.iterator());
    }

}
