package eu.fbk.mpba.sensorsflows;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.IOutputCallback;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.OutputStatus;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * This class adds internal support for the library data-paths.
 * Polls but has a fixed sleep time in the case that each queue is empty.
 */
public abstract class OutputPlugIn<TimeT, ValueT> implements IOutput<TimeT, ValueT> {
//    final String LOG_TAG = "ALE SFW";
    private IOutputCallback<TimeT, ValueT> _manager = null;

    private boolean _stopPending = false;
    private OutputStatus _status = OutputStatus.NOT_INITIALIZED;

    private ArrayBlockingQueue<SensorEventEntry<TimeT, Integer>> _eventsQueue;
    private ArrayBlockingQueue<SensorDataEntry<TimeT, ValueT>> _dataQueue;

    protected OutputPlugIn() {
        int dataQueueCapacity = 80;
        int eventQueueCapacity = 30;
        // FIXME Adjust the capacity
        _eventsQueue = new ArrayBlockingQueue<>(eventQueueCapacity);
        // FIXME Adjust the capacity
        _dataQueue = new ArrayBlockingQueue<>(dataQueueCapacity);
    }

    private Thread _thread = new Thread(new Runnable() {
        @Override
        public void run() {
            pluginInitialize();
            changeState(OutputStatus.INITIALIZED);
            dispatchLoopWhileNotStopPending();
            pluginFinalize();
            changeState(OutputStatus.FINALIZED);
        }
    });

    private void dispatchLoopWhileNotStopPending() {
        SensorDataEntry<TimeT, ValueT> data;
        SensorEventEntry event;
        while (!_stopPending) {
            data = _dataQueue.poll();
            event = _eventsQueue.poll();
            if (data != null)
                newSensorData(data);
            if (event != null)
                newSensorEvent(event);
            else if (data == null)
                try {
                    long sleepInterval = 25; // POI polling time here
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException e) {
//                    Log.w(LOG_TAG, "InterruptedException in OutputImpl.run() find-me:fnh294he97");
                }
        }
    }

    private void changeState(OutputStatus s) {
        if (_manager != null)
            _manager.outputStateChanged(this, _status = s);
    }

    void setOutputCallbackManager(IOutputCallback<TimeT, ValueT> manager) {
        _manager = manager;
    }

    // Implemented Callbacks

    @Override
    public void initialize() {
        changeState(OutputStatus.INITIALIZING);
        _thread.start();
    }

    @Override
    public void finalizeOutput() {
        changeState(OutputStatus.FINALIZING);
        _stopPending = true;
    }

    @Override
    public void sensorStateChanged(ISensor sensor, TimeT time, SensorStatus state) {
    }

    @Override
    public void sensorEvent(ISensor sensor, TimeT time, int type, String message) {
        try {
            // FIXME WARN Locks the sensor's thread
            _eventsQueue.put(new SensorEventEntry<TimeT, Integer>(sensor, time, type, message));
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.sensorEvent() find-me:924nj89f8j2");
        }
    }

    @Override
    public void sensorValue(ISensor sensor, TimeT time, ValueT value) {
        try {
            // FIXME WARN Locks the sensor's thread
            SensorDataEntry<TimeT, ValueT> a = new SensorDataEntry<TimeT, ValueT>(sensor, time, value);
            _dataQueue.put(a);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.sensorValue() find-me:24bhi5ti89");
        }
    }

    // Getters

    @Override
    public OutputStatus getState() {
        return _status;
    }

    // Abstracts to be implemented by the plug-in

    protected abstract void pluginInitialize();

    protected abstract void pluginFinalize();

    protected abstract void newSensorEvent(SensorEventEntry event);

    protected abstract void newSensorData(SensorDataEntry<TimeT, ValueT> data);

    @Override
    public abstract void setLinkedSensors(List<SensorComponent> linkedSensors);
}