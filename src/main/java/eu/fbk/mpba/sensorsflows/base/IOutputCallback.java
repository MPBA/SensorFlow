package eu.fbk.mpba.sensorsflows.base;

public interface IOutputCallback {
    void outputStatusChanged(IOutput sender, OutputStatus status);
}
