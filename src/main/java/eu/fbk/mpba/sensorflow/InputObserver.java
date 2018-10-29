package eu.fbk.mpba.sensorflow;

interface InputObserver {
    void inputStatusChanged(InputManager input, PluginStatus state);
}
