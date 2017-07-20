package eu.fbk.mpba.sensorsflows;

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

    private SFQueue queue;
    private boolean enabled = true;

    OutputManager(Output output, OutputObserver manager) {
        this(output, manager, true);
    }

    OutputManager(Output output, OutputObserver manager, boolean threaded) {
        this.manager = manager;
        outputPlugIn = output;
        this.threaded = threaded;
        if (threaded)
            queue = new SFQueue(outputPlugIn, 800, false);
        else
            // Ugly practice but effective, TODO extract an interface and build another class
            queue = new SFQueue(null, 0, false) {
                @Override
                public void put(Input f, long t, double[] v) throws InterruptedException {
                    outputPlugIn.onValue(f, t, v);
                }

                @Override
                public void put(Input f, long t, String v) throws InterruptedException {
                    outputPlugIn.onLog(f, t, v);
                }

                @Override
                public void putAdded(Input f) throws InterruptedException {
                    outputPlugIn.onInputAdded(f);
                }

                @Override
                public void putRemoved(Input f) throws InterruptedException {
                    outputPlugIn.onInputRemoved(f);
                }
            };
        changeStatus(PluginStatus.INSTANTIATED);
    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            outputPlugIn.onCreate(sessionTag);
            changeStatus(PluginStatus.CREATED);
            linkedInputs.forEach(outputPlugIn::onInputAdded);
            dispatchLoopWhileNotStopPending();
            linkedInputs.forEach(outputPlugIn::onInputRemoved);
            outputPlugIn.onClose();
            changeStatus(PluginStatus.CLOSED);
        }
    });

    private void dispatchLoopWhileNotStopPending() {
        while (!stopPending) {
            try {
                queue.pollToHandler(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) { }
        }
    }

    private void changeStatus(PluginStatus s) {
        if (manager != null)
            manager.outputStatusChanged(this, status = s);
    }

    // Implemented Callbacks

    void onCreate(String sessionTag) {
        if (status == PluginStatus.INSTANTIATED) {
            this.sessionTag = sessionTag;
            if (threaded)
                thread.start();
        } else
            System.out.println("onCreate out of place 4353453ewdr, current status: " + status.toString());
    }

    void onClose() {
        if (status == PluginStatus.CREATED && !stopPending) {
            stopPending = true;
            if (threaded)
                try {
                    thread.join(); // Max time specified in queue pollToHandler call
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } else
            System.out.println("onCreate out of place 4353453ewdr, current status: " + status.toString()
                    + ", stopPending: " + stopPending);
    }

    void pushLog(Input sensor, long time, String message) {
        try {
            // FIXME WARN On full, locks the input's thread
            queue.put(sensor, time, message);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException 238rh2390");
        }
    }

    void pushValue(Input sensor, long time, double[] value) {
        try {
            // FIXME WARN On full, locks the input's thread
            queue.put(sensor, time, value);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException 9234rhyu2");
        }
    }

    // Setters

    synchronized void addInput(Input s) {
        if (linkedInputs.add(s))
            try {
                // FIXME WARN On full, locks the input's thread
                queue.putAdded(s);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException 9234rhyu3");
            }
    }

    synchronized void removeInput(Input s) {
        if (linkedInputs.remove(s))
            try {
                // FIXME WARN On full, locks the input's thread
                queue.putRemoved(s);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException 923w5hyu3");
            }
    }

    synchronized void setEnabled(boolean enabled) {
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
        this.enabled = enabled;
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

    public void close() {
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