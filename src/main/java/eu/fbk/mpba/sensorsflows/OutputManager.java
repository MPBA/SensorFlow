package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
class OutputManager implements IOutput {
    private IOutputCallback _manager = null;

    private boolean _stopPending = false;
    private OutputStatus _status = OutputStatus.NOT_INITIALIZED;
    private Object sessionTag = "unspecified";
    private Output outputPlugIn;
    private Set<ISensor> linkedSensors;

    private ArrayBlockingQueue<SensorEventEntry> _eventsQueue;
    private ArrayBlockingQueue<SensorDataEntry> _dataQueue;
    private boolean enabled = true;

    protected OutputManager(Output output, IOutputCallback manager) {
        _manager = manager;
        linkedSensors = new HashSet<>();
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
        SensorDataEntry data;
        SensorEventEntry event;
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
    public void onStatusChanged(ISensor sensor, long time, SensorStatus state) {
    }

    @Override
    public void onEvent(ISensor sensor, long time, int type, String message) {
        try {
            // FIXME WARN Locks the flow's thread
            _eventsQueue.put(new SensorEventEntry(sensor, time, type, message));
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.onEvent() find-me:924nj89f8j2");
        }
    }

    @Override
    public void onValue(ISensor sensor, long time, double[] value) {
        try {
            // FIXME WARN Locks the flow's thread
            SensorDataEntry a = new SensorDataEntry(sensor, time, value);
            _dataQueue.put(a);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.onValue() find-me:24bhi5ti89");
        }
    }

    // Setters

    public void addFlow(ISensor s) {
        linkedSensors.add(s);
    }

    public void removeFlow(ISensor s) {
        linkedSensors.remove(s);
    }

    // Getters

    @Override
    public OutputStatus getStatus() {
        return _status;
    }

    Output getOutput() {
        return outputPlugIn;
    }

    /**
     * Unregisters every flow linked
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