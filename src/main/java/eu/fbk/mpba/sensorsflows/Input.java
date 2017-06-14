package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;
import eu.fbk.mpba.sensorsflows.util.TimeSource;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class Input implements InputGroup {
    private InputGroup parent;
    private String name;
    private Collection<String> header;
    private Manager manager;
    private Set<OutputManager> outputs = new HashSet<>();

    private boolean listened = true;
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

    protected Input() {
        this(null, Input.class.getSimpleName());
    }

    protected Input(InputGroup parent) {
        this(parent, Input.class.getSimpleName());
    }

    protected Input(InputGroup parent, String name) {
        this(parent, name, null);
    }

    protected Input(InputGroup parent, String name, Collection<String> header) {
        this.parent = parent;
        this.name = name;
        this.header = new ArrayList<>(header);
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
    public void onInputStart() {
        turnOn();
    }

    @Override
    public void onInputStop() {
        turnOff();
    }

    @Override
    public Iterable<Input> getChildren() {
        return Collections.singletonList(this);
    }

    // Listening

    public boolean isListened() {
        return listened;
    }

    public void setListened(boolean listened) {
        this.listened = listened;
    }

    // Notify methods

    public void pushValue(long time, double[] value) {
        getManager().onValue(this, time, value);
    }

    public void pushLog(long time, String message) {
        getManager().onLog(this, time, message);
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

    public abstract void turnOn();

    public abstract void turnOff();

    public enum Status {
        OFF, ON, ERROR
    }
}
