package eu.fbk.mpba.sensorsflows;

import java.util.concurrent.ArrayBlockingQueue;
import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.SensorDataReport;
import eu.fbk.mpba.sensorsflows.base.SensorEventReport;
import eu.fbk.mpba.sensorsflows.base.SensorStatusReport;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class OutputImpl<TimeT, ValueT> implements IOutput<TimeT, ValueT> {
    ArrayBlockingQueue<SensorStatusReport> _statusQueue;
    ArrayBlockingQueue<SensorEventReport> _eventsQueue;
    ArrayBlockingQueue<SensorDataReport> _dataQueue;

    Thread _thread;


    void internalInitialize() {

    }

    void internalFinalize() {

    }

}
