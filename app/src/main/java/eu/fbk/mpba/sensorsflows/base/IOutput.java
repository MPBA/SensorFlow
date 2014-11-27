package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for the data management.
 * TimeT and ValueT must be the same for the whole library.
 */
public interface IOutput<TimeT, ValueT> extends ISensorCallback<ISensor, TimeT, ValueT> {
    public void initialize();

    public void setLinkedSensors();

    public void getStatus();

    public void close();
}
