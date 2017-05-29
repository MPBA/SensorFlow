package eu.fbk.mpba.sensorsflows;

interface OutputObserver {
    void outputStatusChanged(OutputManager sender, OutputManager.Status status);
}
