package eu.fbk.mpba.sensorflow;

interface OutputObserver {
    void outputStatusChanged(OutputManager sender, PluginStatus status);
}
