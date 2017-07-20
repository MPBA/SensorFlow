package eu.fbk.mpba.sensorflow;

/**
 * Main interface for the device's data transport.
 */
interface InputObserver {
    void inputStatusChanged(InputManager input, PluginStatus state);
}
