package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.TreeSet;
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
class OutputManager<TimeT, ValueT> implements IOutput<TimeT, ValueT> {
    private IOutputCallback<TimeT, ValueT> _manager = null;

    private boolean _stopPending = false;
    private OutputStatus _status = OutputStatus.NOT_INITIALIZED;
    private Object sessionTag = "unspecified";
    private Output<TimeT, ValueT> outputPlugIn;
    private TreeSet<ISensor> linkedSensors;

    private ArrayBlockingQueue<SensorEventEntry<TimeT>> _eventsQueue;
    private ArrayBlockingQueue<SensorDataEntry<TimeT, ValueT>> _dataQueue;
    private boolean enabled = true;

    protected OutputManager(Output<TimeT, ValueT> output, IOutputCallback<TimeT, ValueT> manager) {
        _manager = manager;
        linkedSensors = new TreeSet<>();
        outputPlugIn = output;
        int dataQueueCapacity = 100;
        int eventsQueueCapacity = 50;
        // TODO POI Adjust the capacity
        _eventsQueue = new ArrayBlockingQueue<>(eventsQueueCapacity);
        // TODO POI Adjust the capacity
        _dataQueue = new ArrayBlockingQueue<>(dataQueueCapacity);
    }

    private Thread _thread = new Thread(new Runnable() {
        @Override
        public void run() {
            outputPlugIn.onOutputStart(sessionTag, new ArrayList<>(linkedSensors));
            changeStatus(OutputStatus.INITIALIZED);
            dispatchLoopWhileNotStopPending();
            outputPlugIn.onOutputStop();
            changeStatus(OutputStatus.FINALIZED);
        }
    });

    private void dispatchLoopWhileNotStopPending() {
        SensorDataEntry<TimeT, ValueT> data;
        SensorEventEntry<TimeT> event;
        while (true) {
            data = _dataQueue.poll();
            event = _eventsQueue.poll();
            if (data != null)
                outputPlugIn.onValue(data);
            if (event != null)
                outputPlugIn.onEvent(event);
            else if (data == null)
                if (_stopPending)
                    break;
                else
                    try {
                        long sleepInterval = 50; // TODO POI polling timestamp here
                        Thread.sleep(sleepInterval);
                    } catch (InterruptedException e) {
//                    Log.w(LOG_TAG, "InterruptedException in OutputImpl.run() find-me:fnh294he97");
                    }
        }
    }

    private void changeStatus(OutputStatus s) {
        if (_manager != null)
            _manager.outputStatusChanged(this, _status = s);
    }

    // Implemented Callbacks

    @Override
    public void initializeOutput(Object sessionTag) {
        this.sessionTag = sessionTag;
        changeStatus(OutputStatus.INITIALIZING);
        // outputPlugIn.onOutputStart(...) in _thread
        _thread.start();
    }

    @Override
    public void finalizeOutput() {
        changeStatus(OutputStatus.FINALIZING);
        _stopPending = true;
        try {
            _thread.join(); // FIXME POI Indefinite wait
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(ISensor sensor, TimeT time, SensorStatus state) {
    }

    @Override
    public void onEvent(ISensor sensor, TimeT time, int type, String message) {
        try {
            // FIXME WARN Locks the sensor's thread
            _eventsQueue.put(new SensorEventEntry<>(sensor, time, type, message));
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.onEvent() find-me:924nj89f8j2");
        }
    }

    @Override
    public void onValue(ISensor sensor, TimeT time, ValueT value) {
        try {
            // FIXME WARN Locks the sensor's thread
            SensorDataEntry<TimeT, ValueT> a = new SensorDataEntry<>(sensor, time, value);
            _dataQueue.put(a);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.onValue() find-me:24bhi5ti89");
        }
    }

    // Setters

    public void addFlow(ISensor s) {
        linkedSensors.add(s);
    }

    // Getters

    @Override
    public OutputStatus getStatus() {
        return _status;
    }

    Output<TimeT, ValueT> getOutput() {
        return outputPlugIn;
    }

    /**
     * Unregisters every sensor linked
     */
    public void close() {
        linkedSensors.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}