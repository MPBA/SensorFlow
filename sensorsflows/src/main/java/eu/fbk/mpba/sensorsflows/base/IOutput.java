package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for the data management.
 * TimeT and ValueT must be the same for the whole library.
 */
public interface IOutput<TimeT, ValueT> extends ISensorDataCallback<ISensor, TimeT, ValueT> {

    public void initialize(Object sessionTag);

    public OutputStatus getState();

    public void finalizeOutput();
}
