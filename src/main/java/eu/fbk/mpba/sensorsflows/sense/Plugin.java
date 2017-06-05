package eu.fbk.mpba.sensorsflows.sense;

/**
 * Base class for every sense plugin. It implements a common interface
 */
public abstract class Plugin implements eu.fbk.mpba.sensorsflows.Plugin {
    private String settings;
    private String name = getClass().getSimpleName();

    /**
     * Returns the name of the module as an acquisition-unique identification. It should be standard
     * and well-known as it identifies the module.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the module as an acquisition-unique identification. It should be standard
     * and well-known as it identifies the module.
     * If called while isFlowing is true it throws an exception, as the name can not change during
     * the acquisition.
     *
     * For example it might be retrieved from the configuration string in getSettings().
     * @param name The identifier
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * True when the acquisition is in progress i.e. between the start and stop calls. When it
     * returns true, some attributes of the module can not be altered.
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
    public Plugin(String settings) {
        this.settings = settings;
    }

    /**
     * Returns the configuration string set by the constructor.
     * @return String, as-is.
     */
    public String getSettings() {
        return settings;
    }
}
