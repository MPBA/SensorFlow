package eu.fbk.mpba.sensorflow;

/**
 * Enumerates the possible statuses of a plugin.
 *
 * INSTANTIATED: The plugin has been instantiated but not completely initialized.
 * CREATED: The plugin has been completely initialized and is ready to be added to SensorFlow.
 * ADDED: The plugin has been successfully added to SensorFlow and can send/receive data.
 * REMOVED: The plugin has been just removed from SensorFlow.
 * CLOSED: The plugin has been closed and its resources freed. No longer usable.
 */
public enum PluginStatus {
    /**
     * The first state, the onCreate has not been called yet.
     */
    INSTANTIATED,
    /**
     * The onCreate call succeeded.
     */
    CREATED,
    /**
     * The plugin has been added to SensorFlow and the onAdded call succeeded.
     */
    ADDED,
    /**
     * The plugin has been removed from SensorFlow and the call to onRemove succeeded.
     */
    REMOVED,
    /**
     * The method onClose has been called and the resources should have been freed.
     */
    CLOSED
}
