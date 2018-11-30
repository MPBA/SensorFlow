package eu.fbk.mpba.sensorflow;

public interface SFPlugin {
    String getName();

    /**
     * Call this to close the plugin and free all its resources.
     */
    void onClose();
}
