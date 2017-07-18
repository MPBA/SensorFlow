package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class adds internal support for the library data-paths.
 * Polls but has a fixed sleep time in the case that each queue is empty.
 */
class OutputManager {
    private OutputObserver _manager = null;

    private boolean _stopPending = false;
    private Status _status = Status.NOT_INITIALIZED;
    private String sessionTag = "unspecified";
    private Output outputPlugIn;
    private Set<Input> linkedSensors;

    private FlowQueue _queue;
    private boolean enabled = true;

    OutputManager(Output output, OutputObserver manager) {
        _manager = manager;
        linkedSensors = new HashSet<>();
        outputPlugIn = output;
        int queueCapacity = 200;
        _queue = new FlowQueue(outputPlugIn, queueCapacity, false);
    }

    private Thread _thread = new Thread(new Runnable() {
        @Override
        public void run() {
            outputPlugIn.onOutputStart(sessionTag, new ArrayList<>(linkedSensors));
            changeStatus(Status.INITIALIZED);
            dispatchLoopWhileNotStopPending();
            outputPlugIn.onOutputStop();
            changeStatus(Status.FINALIZED);
        }
    });

    private void dispatchLoopWhileNotStopPending() {
        while (!_stopPending) {
            try {
                _queue.pollToHandler(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) { }
        }
    }

    private void changeStatus(Status s) {
        if (_manager != null)
            _manager.outputStatusChanged(this, _status = s);
    }

    // Implemented Callbacks

    void initializeOutput(String sessionTag) {
        this.sessionTag = sessionTag;
        changeStatus(Status.INITIALIZING);
        // outputPlugIn.onOutputStart(...) in _thread
        _thread.start();
    }

    void finalizeOutput() {
        changeStatus(Status.FINALIZING);
        _stopPending = true;
        try {
            _thread.join(); // Max time specified in queue pollToHandler call
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void onLog(Input sensor, long time, String message) {
        try {
            // FIXME WARN On full, locks the flow's thread
            _queue.put(sensor, time, message);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.pushLog() find-me:924nj89f8j2");
        }
    }

    void onValue(Input sensor, long time, double[] value) {
        try {
            // FIXME WARN On full, locks the flow's thread
            _queue.put(sensor, time, value);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.pushValue() find-me:24bhi5ti89");
        }
    }

    // Setters

    void addFlow(Input s) {
        linkedSensors.add(s);
    }

    void removeFlow(Input s) {
        linkedSensors.remove(s);
    }

    // Getters

    Status getStatus() {
        return _status;
    }

    Output getOutput() {
        return outputPlugIn;
    }

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

    public enum Status {
        NOT_INITIALIZED, INITIALIZING, INITIALIZED, FINALIZING, FINALIZED
    }
}