package eu.fbk.mpba.sensorflow.sense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;

/**
 * This is the base class for an input plugin.
 */
public abstract class InputModule extends Module {
    private final ArrayList<Stream> children = new ArrayList<>();
    private boolean added = false;

    /**
     * Constructor of abstract class LOL
     */
    protected InputModule() { }

    /**
     * Adds a Stream to this input plugin.
     *
     * @param input The stream to add to this input plugin.
     */
    protected void addStream(Stream input) {
        if (!added) {
            children.add(input);
        } else {
            throw new UnsupportedOperationException("Online schema changes are not supported. Add streams before onAdded or after onRemoved.");
        }
    }

    protected Collection<Stream> getStreams() {
        return children;
    }

    /**
     * Returns a list containing the Streams (and Inputs) of this input plugin. The instances are
     * returned as Inputs because ModuleLog and ModuleStatus are not Streams, just Inputs.
     *
     * @return A list with the streams and the Inputs of the plugin.
     */
    @Override
    public final Collection<Input> getChildren() {
        ArrayList<Input> sfPlugins = new ArrayList<>(super.getChildren());
        sfPlugins.addAll(children);
        return Collections.unmodifiableCollection(sfPlugins);
    }

    /**
     * If overridden enables an initialization code to be executed before being added to SensorFlow.
     */
    public abstract void onCreate();

    /**
     * If overridden enables code to be executed after being added to SensorFlow.
     */
    public void onAdded() { // TODO: Bug, this method must be called by overriders
        added = true;
    }

    /**
     * If overridden enables code to be executed after being removed from SensorFlow.
     */
    public void onRemoved() { // TODO: Bug, this method must be called by overriders
        added = false;
    }

    /**
     * If overridden enables an initialization code to be executed before being added to SensorFlow.
     */
    public abstract void onClose();
}
