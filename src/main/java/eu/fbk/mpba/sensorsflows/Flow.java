package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;
import eu.fbk.mpba.sensorsflows.util.TimeSource;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class Flow {
    private Input _parent = null;
    private List<FlowObserver> _handler = new ArrayList<>();
    private Set<OutputManager> _outputs = new HashSet<>();

    private boolean mMuted = false;
    protected Status mStatus = Status.OFF;
    private static long _bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();
    private static TimeSource _time = new TimeSource() {

        @Override
        public long getMonoUTCNanos() {
            return System.nanoTime() + _bootTime;
        }

        @Override
        public long getMonoUTCNanos(long realTimeNanos) {
            return realTimeNanos + _bootTime;
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
        _parent = parent;
    }

    void addOutput(OutputManager _output) {
        _outputs.add(_output);
    }

    void removeOutput(OutputManager _output) {
        _outputs.remove(_output);
    }

    void addHandler(FlowObserver man) {
        _handler.add(man);
    }

    void removeHandler(FlowObserver man) {
        _handler.remove(man);
    }

    Iterable<OutputManager> getOutputs() {
        return new ReadOnlyIterable<>(_outputs.iterator());
    }

    // Managed protected getters setters

    protected void changeStatus(Status state) {
        for (FlowObserver i : _handler) {
//            if (i instanceof SensorFlow && ((SensorFlow)i).getStatus() == Status.CLOSED)
//                _handler.remove(i);
            i.onStatusChanged(this, Integer.MIN_VALUE, mStatus = state);
        }
    }

    /**
     * Unregisters every outputDecorator
     */
    public void close() {
        _handler.clear();
        _outputs.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    // Managed Overrides

    public Input getParentInput() {
        return _parent;
    }

    public Status getStatus() {
        return mStatus;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public static TimeSource getTimeSource() {
        return _time;
    }

    // Notify methods

    public void onValue(long time, double[] value) {
        for (FlowObserver i : _handler) {
//            if (i instanceof SensorFlow && ((SensorFlow)i).getStatus() == Status.CLOSED)
//                _handler.remove(i);
            i.onValue(this, time, value);
        }
    }

    public void onLog(long time, int type, String message) {
        for (FlowObserver i : _handler) {
//            if (i instanceof SensorFlow && ((SensorFlow)i).getStatus() == Status.CLOSED)
//                _handler.remove(i);
            i.onEvent(this, time, type, message);
        }
    }

    // Listening

    public boolean isMuted() {
        return mMuted;
    }

    public void setMuted(boolean muted) {
        this.mMuted = muted;
    }

    // To implement

    public abstract List<Object> getHeader();

    public abstract void switchOnAsync();

    public abstract void switchOffAsync();

    public enum Status {
        OFF, ON, ERROR
    }
}
