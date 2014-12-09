package eu.fbk.mpba.sensorsflows.base;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class SensorImpl implements ISensor<DeviceImpl> {
    private boolean __listened = false;

    public boolean isListened() {
        return __listened;
    }

    public void setListened(boolean _listening) {
        this.__listened = _listening;
    }
}
