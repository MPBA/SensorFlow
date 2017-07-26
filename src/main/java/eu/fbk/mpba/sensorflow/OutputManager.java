package eu.fbk.mpba.sensorflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class OutputManager {
    private OutputObserver manager = null;

    private boolean stopPending = false;
    private PluginStatus status = PluginStatus.INSTANTIATED;
    private String sessionTag;
    private Output outputPlugIn;
    private boolean threaded;
    private Set<Input> linkedInputs = new HashSet<>();
    private Set<Input> linkedInputsSnapshot = new HashSet<>();

    private Output queue;
    private volatile boolean enabled = true;

    OutputManager(Output output, OutputObserver manager, boolean buffered) {
        this.manager = manager;
        outputPlugIn = output;
        this.threaded = buffered;
        if (buffered)
            queue = new OutputBuffer(outputPlugIn, 800, false);
        else
            queue = outputPlugIn;
        setEnabled(false);
        changeStatus(PluginStatus.INSTANTIATED);
    }

    private Thread sbufferingThread = new Thread(() -> {
        try {
            OutputBuffer queue = (OutputBuffer)OutputManager.this.queue;
            while (!stopPending) {
                queue.pollToHandler(100, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ignored) { }
    });

    private void beforeDispatch() {
        // Create plugin
        outputPlugIn.onCreate(sessionTag);
        changeStatus(PluginStatus.CREATED);
        // Here input add/removal differentially buffered
        // Enable plugin
        //   Here every linked input will be added
        //   This call is sync: mutexes with addInput and removeInput
        setEnabled(true);
        // Here input add/removal in queue
    }

    private void afterDispatch() {
        setEnabled(false);
        // Here input add/removal differentially buffered, but no more useful
        linkedInputsSnapshot.forEach(outputPlugIn::onInputRemoved);
        outputPlugIn.onClose();
        changeStatus(PluginStatus.CLOSED);
    }

    private void changeStatus(PluginStatus s) {
        if (manager != null)
            manager.outputStatusChanged(this, status = s);
    }

    // Implemented

    void onCreateAndStart(String sessionTag) {
        if (status == PluginStatus.INSTANTIATED) {
            this.sessionTag = sessionTag;
            beforeDispatch();
            if (threaded)
                sbufferingThread.start();
        } else
            System.out.println("onCreateAndStart out of place 4353453ewdr, current status: " + status.toString());
    }

    void onStopAndClose() {
        if (status == PluginStatus.CREATED && !stopPending) {
            stopPending = true;
            if (threaded)
                try {
                    sbufferingThread.join(); // Max time specified in queue pollToHandler call
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            afterDispatch();
        } else
            System.out.println("onCreateAndStart out of place 4353453ewdr, current status: " + status.toString()
                    + ", stopPending: " + stopPending);
    }

    void pushLog(Input sensor, long time, String message) {
        try {
            queue.onLog(sensor, time, message);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof InterruptedException)
                System.out.println("InterruptedException 9234rhyu1");
            else throw e;
        }
    }

    void pushValue(Input sensor, long time, double[] value) {
        try {
            queue.onValue(sensor, time, value);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof InterruptedException)
                System.out.println("InterruptedException 9234rhyu2");
            else throw e;
        }
    }

    // Setters

    synchronized void addInput(Input s) {
        if (linkedInputs.add(s) && enabled)
            try {
                queue.onInputAdded(s);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException)
                    System.out.println("InterruptedException 9234rhyu3");
                else throw e;
            }
    }

    synchronized void removeInput(Input s) {
        if (linkedInputs.remove(s) && enabled)
            try {
                queue.onInputRemoved(s);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof InterruptedException)
                    System.out.println("InterruptedException 9234rhyu4");
                else throw e;
            }
    }

    synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            linkedInputsSnapshot.clear();
            linkedInputsSnapshot.addAll(linkedInputs);
        } else {
            HashSet<Input> toRemove = new HashSet<>(linkedInputsSnapshot);
            toRemove.removeAll(linkedInputs);
            HashSet<Input> toAdd = new HashSet<>(linkedInputs);
            toAdd.removeAll(linkedInputsSnapshot);

            linkedInputs.clear();
            linkedInputs.addAll(linkedInputsSnapshot);

            toRemove.forEach(this::removeInput);
            toAdd.forEach(this::addInput);
        }
    }

    // Getters

    Collection<Input> getInputs() {
        ArrayList<Input> copy = new ArrayList<>(linkedInputs);
        return Collections.unmodifiableCollection(copy);
    }

    boolean isEnabled() {
        return enabled;
    }

    PluginStatus getStatus() {
        return status;
    }

    Output getOutput() {
        return outputPlugIn;
    }

    // Finalization

    public synchronized void close() {
        if (outputPlugIn != null) {
            outputPlugIn.onClose();
            outputPlugIn = null;
        }
        linkedInputs.clear();
        linkedInputs = null;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}