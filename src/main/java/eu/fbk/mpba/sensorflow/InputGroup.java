package eu.fbk.mpba.sensorflow;

import java.util.Collection;

public interface InputGroup extends SFPlugin {

    /**
     * Called when the input plugin is added to SensorFlow but not wired yet. This is the place
     * where to finalize the setup of the Streams.
     */
    void onCreate();

    /**
     * Called after routing ("wiring") the plugin to all the outputs.
     */
    void onAdded();

    /**
     * Called when the plugin is removed from SensorFlow
     */
    void onRemoved();

    Collection<Input> getChildren();

    String getSimpleName();

}
