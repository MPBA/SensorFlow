package eu.fbk.mpba.sensorsflows.base;

public interface IOutputCallback<TimeT, ValueT> {
    void outputStatusChanged(IOutput<TimeT, ValueT> sender, OutputStatus status);
}
