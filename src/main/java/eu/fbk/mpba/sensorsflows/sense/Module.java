package eu.fbk.mpba.sensorsflows.sense;

import java.util.ArrayList;
import java.util.Collection;

import eu.fbk.mpba.sensorsflows.NamedPlugin;

/**
 * Base class for every sense plugin. It implements a common interface
 */
public abstract class Module implements NamedPlugin {
    private final LogInput moduleLog;
    private final String settings;
    private final String simpleName = getClass().getSimpleName();
    private final ArrayList<NamedPlugin> sfChildren = new ArrayList<>(4);

    void addSFChild(NamedPlugin child) {
        sfChildren.add(child);
    }

    Collection<NamedPlugin> getSFChildren() {
        return sfChildren;
    }

    /**
     * Returns the path of the module considering InputGroups.
     */
    public String getName() {
        return simpleName;
    }

    /**
     * Returns the name of the module as an acquisition-unique identification. It should be standard
     * and well-known as it identifies the module.
     */
    public String getSimpleName() {
        return simpleName;
    }

    /**
     * True when the acquisition is in progress i.e. between the start and stop calls. When it
     * returns true, some attributes of the InputModule can not be altered.
     *
     * @return True if it is flowing, false otherwise.
     */
    public abstract boolean isFlowing();

    /**
     * This method is called when the acquisition is stopping. After this method returns, data can
     * be pushed, and the method isFlowing returns false.
     */
    protected abstract void stop();

    /**
     * This method is called when SensorFlow is asked to free the resources.
     */
    public abstract void close();

    /**
     * Constructor of abstract class
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    Module(String name, String settings) {
        this.settings = settings;
        this.moduleLog = new LogInput(this);
        addSFChild(moduleLog);
    }

    /**
     * Returns the configuration string set by the constructor.
     * @return String, as-is.
     */
    public String getSettings() {
        return settings;
    }

    /**
     * Custom logs from the device software/hardware. Should contain information about the hardware
     * and firmware versions for reproducibility (TODO: Standard enumeration of codes)
     * @param type Identification code of the log type
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    protected void pushLog(int type, String tag, String message) {
        moduleLog.pushLog(type, tag, message);
    }
}
