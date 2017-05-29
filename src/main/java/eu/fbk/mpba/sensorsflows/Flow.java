package eu.fbk.mpba.sensorsflows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;
import eu.fbk.mpba.sensorsflows.util.TimeSource;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class Flow {
    private Input parent;
    private Manager manager;
    private Set<OutputManager> outputs = new HashSet<>();

    private boolean muted = false;
    protected Status status = Status.OFF;
    private static long bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();
    private static TimeSource time = new TimeSource() {

        @Override
        public long getMonoUTCNanos() {
            return System.nanoTime() + bootTime;
        }

        @Override
        public long getMonoUTCNanos(long realTimeNanos) {
            return realTimeNanos + bootTime;
        }

        @Override
        public long getMonoUTCMillis() {
            return getMonoUTCNanos() / 1_000_000;
        }

        @Override
        public long getMonoUTCMillis(long realTimeNanos) {
            return getMonoUTCNanos(realTimeNanos) / 1_000_000;
        }
    };

    protected Flow(Input parent) {
        this.parent = parent;
    }

    void addOutput(OutputManager _output) {
        outputs.add(_output);
    }

    void removeOutput(OutputManager _output) {
        outputs.remove(_output);
    }

    void setHandler(Manager man) {
        manager = man;
    }

    Manager getManager() {
        return manager;
    }

    Iterable<OutputManager> getOutputs() {
        return new ReadOnlyIterable<>(outputs.iterator());
    }

    // Managed protected getters setters

    protected void changeStatus(Status state) {
        manager.onStatusChanged(this, Integer.MIN_VALUE, status = state);
    }

    /**
     * Unregisters every outputDecorator
     */
    public void close() {
        outputs.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    // Managed Overrides

    public Input getParentInput() {
        return parent;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public static TimeSource getTimeSource() {
        return time;
    }

    // Listening

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    // Notify methods

    public void onValue(long time, double[] value) {
        getManager().onValue(this, time, value);
    }

    public void onLog(long time, String message) {
        getManager().onLog(this, time, message);
    }

    // To implement

    public abstract String[] getHeader();

    public abstract void switchOn();

    public abstract void switchOff();

    public enum Status {
        OFF, ON, ERROR
    }
}
