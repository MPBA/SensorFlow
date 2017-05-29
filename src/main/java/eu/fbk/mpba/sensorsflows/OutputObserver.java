package eu.fbk.mpba.sensorsflows;

interface OutputObserver {
    void outputStatusChanged(OutputManager sender, Output.Status status);
}
