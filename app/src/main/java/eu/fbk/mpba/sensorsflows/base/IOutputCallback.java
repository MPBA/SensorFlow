package eu.fbk.mpba.sensorsflows.base;

public interface IOutputCallback<TimeT, ValueT> {
    public void outputStateChanged(IOutput<TimeT, ValueT> sender, OutputStatus status);
}
