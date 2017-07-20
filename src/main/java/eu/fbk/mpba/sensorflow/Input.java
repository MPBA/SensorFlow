package eu.fbk.mpba.sensorflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class Input implements InputGroup {
    private boolean listened = true;
    private String name;
    protected Status status = Status.OFF;

    private InputGroup parent;
    private DataObserver manager;
    private Collection<String> header;
    private Set<OutputManager> outputs = new HashSet<>();

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

    protected Input(Collection<String> header) {
        this(null, Input.class.getSimpleName(), header);
    }

    protected Input(InputGroup parent, Collection<String> header) {
        this(parent, Input.class.getSimpleName(), header);
    }

    protected Input(InputGroup parent, String name, Collection<String> header) {
        this.parent = parent;
        this.name = name != null ? name : getClass().getSimpleName() + "-" + hashCode();
        this.header = new ArrayList<>(header);
    }

    void addOutput(OutputManager output) {
        outputs.add(output);
    }

    void removeOutput(OutputManager output) {
        outputs.remove(output);
    }

    void setManager(DataObserver man) {
        manager = man;
    }

    Collection<OutputManager> getOutputs() {
        return Collections.unmodifiableSet(outputs);
    }

    // Managed protected getters setters

    protected void changeStatus(Status state) {
        // Not notified to SF
        status = state;
    }

    public void close() {
        outputs.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    // Managed Overrides

    public InputGroup getParent() {
        return parent;
    }

    public Status getStatus() {
        return status;
    }

    public static TimeSource getTimeSource() {
        return time;
    }

    @Override
    public Iterable<Input> getChildren() {
        return Collections.singletonList(this);
    }

    // Listening

    public boolean isMuted() {
        return !listened;
    }

    public void mute() {
        this.listened = false;
    }

    public void unmute() {
        this.listened = true;
    }

    // Notify methods

    public void pushValue(long time, double[] value) {
        // Shouldn't be called before onCreate
        manager.onValue(this, time, value);
    }

    public void pushLog(long time, String message) {
        // Shouldn't be called before onCreate
        manager.onLog(this, time, message);
    }

    // To be implemented

    public String getName() {
        InputGroup parent = getParent();
        return parent != null ? parent.getName() + "/" + getSimpleName() : getSimpleName();
    }

    public String getSimpleName() {
        return name;
    }

    public Collection<String> getHeader(){
        return header;
    }

    public enum Status {
        OFF, ON, ERROR
    }
}
