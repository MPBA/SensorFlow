package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.Enumeration;

import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.util.IterToEnum;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorImpl<TypeT, ValueT> implements ISensor<DeviceImpl> {
    private boolean _listened = false;
    private ArrayList<IOutput> _outputs;
    private int _avgStreamingInterval = 0;

    public boolean isListened() {
        return _listened;
    }
    public void setListened(boolean listened) {
        this._listened = listened;
    }

    Enumeration<IOutput> getOutputs() {
        return new IterToEnum<IOutput>(_outputs.iterator());
    }

    void setOutputs(ArrayList<IOutput> _outputs) {
        this._outputs = _outputs;
    }

}
