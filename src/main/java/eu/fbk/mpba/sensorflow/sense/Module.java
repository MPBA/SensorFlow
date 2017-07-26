package eu.fbk.mpba.sensorflow.sense;

import java.util.ArrayList;
import java.util.Collection;

import eu.fbk.mpba.sensorflow.SFPlugin;

/**
 * Base class for every sense plugin. It implements a common interface
 */
public abstract class Module implements SFPlugin {
    private final LogInput moduleLog;
    private final String configuration;
    private final String simpleName;
    private final ArrayList<SFPlugin> sfChildren = new ArrayList<>(4);

    /**
     * Constructor of abstract class
     * @param configuration Configuration string (e.g. json) to be passed to the Module.
     */
    Module(String name, String configuration) {
        this.configuration = configuration;
        this.moduleLog = new LogInput(name);
        simpleName = name == null ? getClass().getSimpleName() : name;
        addSFChild(moduleLog);
    }

    void addSFChild(SFPlugin child) {
        sfChildren.add(child);
    }

    Collection<SFPlugin> getSFChildren() {
        return sfChildren;
    }

    /**
     * Returns the path of the module considering InputGroups.
     */
    @Override
    public String getName() {
        return getSimpleName();
    }

    /**
     * Returns the name of the module as an acquisition-unique identification. It should be standard
     * and well-known as it identifies the module.
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the configuration string set by the constructor.
     * @return String, as-is.
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * Custom logs from the device software/hardware. Should contain information about the hardware
     * and firmware versions for reproducibility
     * @param type Identification code of the log type, 0 for metadata (TYPE_METADATA)
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    protected void pushLog(int type, String tag, String message) {
        moduleLog.pushLog(type, tag, message);
    }

    public static final int TYPE_METADATA = 0;

    public static final String TAG_DEV_ID = "DEV_ID";
    public static final String TAG_DEV_INFO = "DEV_INFO";
    public static final String TAG_HW_VER = "DEV_HW";
    public static final String TAG_SW_VER = "DEV_SW";
}
