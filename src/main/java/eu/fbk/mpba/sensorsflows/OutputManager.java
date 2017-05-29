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
    private Output.Status _status = Output.Status.NOT_INITIALIZED;
    private Object sessionTag = "unspecified";
    private Output outputPlugIn;
    private Set<Flow> linkedSensors;

    private FlowBuffer _queue;
    private boolean enabled = true;

    protected OutputManager(Output output, OutputObserver manager) {
        _manager = manager;
        linkedSensors = new HashSet<>();
        outputPlugIn = output;
        int queueCapacity = 200;
        _queue = new FlowBuffer(outputPlugIn, queueCapacity, false);
    }

    private Thread _thread = new Thread(new Runnable() {
        @Override
        public void run() {
            outputPlugIn.onOutputStart(sessionTag, new ArrayList<>(linkedSensors));
            changeStatus(Output.Status.INITIALIZED);
            dispatchLoopWhileNotStopPending();
            outputPlugIn.onOutputStop();
            changeStatus(Output.Status.FINALIZED);
        }
    });

    private void dispatchLoopWhileNotStopPending() {
        while (!_stopPending) {
            try {
                _queue.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) { }
        }
    }

    private void changeStatus(Output.Status s) {
        if (_manager != null)
            _manager.outputStatusChanged(this, _status = s);
    }

    // Implemented Callbacks

    void initializeOutput(Object sessionTag) {
        this.sessionTag = sessionTag;
        changeStatus(Output.Status.INITIALIZING);
        // outputPlugIn.onOutputStart(...) in _thread
        _thread.start();
    }

    void finalizeOutput() {
        changeStatus(Output.Status.FINALIZING);
        _stopPending = true;
        try {
            _thread.join(); // Max time specified in queue poll call
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void onStatusChanged(Flow sensor, long time, Flow.Status state) {
        onEvent(sensor, time, 0, "onStatusChanged " + state.toString());
    }

    void onEvent(Flow sensor, long time, int type, String message) {
        try {
            // FIXME WARN On full, locks the flow's thread
            _queue.put(sensor, time, type, message);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.onEvent() find-me:924nj89f8j2");
        }
    }

    void onValue(Flow sensor, long time, double[] value) {
        try {
            // FIXME WARN On full, locks the flow's thread
            _queue.put(sensor, time, value);
        } catch (InterruptedException e) {
//            Log.w(LOG_TAG, "InterruptedException in OutputImpl.onValue() find-me:24bhi5ti89");
        }
    }

    // Setters

    void addFlow(Flow s) {
        linkedSensors.add(s);
    }

    void removeFlow(Flow s) {
        linkedSensors.remove(s);
    }

    // Getters

    Output.Status getStatus() {
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