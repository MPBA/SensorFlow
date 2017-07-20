package eu.fbk.mpba.sensorsflows;

interface OutputObserver {
    void outputStatusChanged(OutputManager sender, PluginStatus status);
}
