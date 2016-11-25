package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for the data management.
 * TimeT and ValueT must be the same for the whole library.
 */
public interface IOutput extends IFlowCallback<ISensor> {

    void initializeOutput(Object sessionTag);

    OutputStatus getStatus();

    void finalizeOutput();

    void close();
}
