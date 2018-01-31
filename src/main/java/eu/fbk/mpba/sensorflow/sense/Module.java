package eu.fbk.mpba.sensorflow.sense;

import java.util.ArrayList;
import java.util.Collection;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;
import eu.fbk.mpba.sensorflow.SFPlugin;

/**
 * Base class for every sense plugin. It implements a common interface
 */
public abstract class Module implements SFPlugin, InputGroup {
    private final LogInput moduleLog;
    private final StatusInput moduleStatus;
    private final ArrayList<Input> sfChildren = new ArrayList<>(4);
    private String simpleName;
    private String configuration;

    /**
     * Constructor of abstract class
     */
    Module() {
        simpleName = getClass().getSimpleName();
        moduleLog = new LogInput(this, "ModuleLog");
        moduleStatus = new StatusInput(this, "ModuleStatus");
        addSFChild(moduleLog);
        addSFChild(moduleStatus);
    }

    private void addSFChild(Input child) {
        sfChildren.add(child);
    }

    /**
     * Returns the path of the module considering InputGroups.
     */
    @Override
    public final String getName() {
        return getSimpleName();
    }

    /**
     * Returns the name of the module as an acquisition-unique identification. It should be standard
     * and well-known as it identifies the module.
     */
    public final String getSimpleName() {
        return simpleName;
    }

    public final void setName(String simpleName) {
        this.simpleName = simpleName == null ? getClass().getSimpleName() : simpleName;
//        moduleLog.setName(getName());
    }

    public final void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the configuration string set by the constructor.
     * @return String, as-is.
     */
    protected String getConfiguration() {
        return configuration;
    }

    /**
     * Custom logs from the device software/hardware.
     * @param type Identification code of the log type, 0 for metadata (LOG_*)
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    protected void pushLog(int type, String tag, String message) {
        moduleLog.pushLog(type, tag, message);
    }

    public enum Status {
        OFF, ERROR, WAIT, OK
    }

    /**
     * Updates the status of the Module. Produces also a log with code LOG_STATUS_UPATE and the
     * status as tag, the message is empty.
     * @param status New status to propagate.
     */
    protected void pushStatus(Status status) {
        pushLog(LOG_STATUS_UPDATE, status.name(), "");
        moduleStatus.pushStatus(status);
    }

    protected static final int LOG_METADATA = 201;
    protected static final int LOG_UI = 202;
    public static final int LOG_FAILURE = 106;
    protected static final int LOG_E = 105;
    protected static final int LOG_W = 104;
    protected static final int LOG_D = 103;
    public static final int LOG_I = 102;
    public static final int LOG_V = 101;
    private static final int LOG_STATUS_UPDATE = 64;

    public void putKeyValue(String key, String value) {
        moduleLog.putKeyValue(key, value);
    }

    @Override
    protected void finalize() throws Throwable {
        onClose();
        super.finalize();
    }

    protected static final String KEY_DEV_ID = "DEV_ID";
    protected static final String KEY_DEV_INFO = "DEV_INFO";
    protected static final String KEY_HW_VER = "DEV_HW";
    public static final String KEY_SW_VER = "DEV_SW";


    @Override
    public void onCreate() { }

    @Override
    public void onAdded() { }

    @Override
    public void onRemoved() { }

    @Override
    public Collection<Input> getChildren() {
        return sfChildren;
    }
}
