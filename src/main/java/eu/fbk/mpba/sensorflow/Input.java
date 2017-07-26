package eu.fbk.mpba.sensorflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class adds internal support for the library data-paths.
 */
public abstract class Input implements InputGroup {
    private boolean listened = true;
    private String name;
    protected Status status = Status.OFF;

    private InputGroup parent;
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

    private ReentrantReadWriteLock outputsAccess = new ReentrantReadWriteLock(false);

    void addOutput(OutputManager output) {
        outputsAccess.writeLock().lock();
        outputs.add(output);
        outputsAccess.writeLock().unlock();
    }

    void removeOutput(OutputManager output) {
        outputsAccess.writeLock().lock();
        outputs.remove(output);
        outputsAccess.writeLock().unlock();
    }

    Collection<OutputManager> getOutputs() {
        outputsAccess.readLock().lock();
        ArrayList<OutputManager> outputManagers = new ArrayList<>(outputs);
        outputsAccess.readLock().unlock();
        return outputManagers;
    }

    // Managed protected getters setters

    protected void changeStatus(Status state) {
        // Not notified to SF
        status = state;
    }

    public void close() {
        outputsAccess.writeLock().lock();
        outputs.clear();
        outputsAccess.writeLock().unlock();
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
        outputsAccess.readLock().lock();
        outputs.stream()
                .filter(OutputManager::isEnabled)
                .forEach(o -> o.pushValue(this, time, value));
        outputsAccess.readLock().unlock();

    }

    public void pushLog(long time, String message) {
        // Shouldn't be called before onCreate
        outputsAccess.readLock().lock();
        outputs.stream()
                .filter(OutputManager::isEnabled)
                .forEach(o -> o.pushLog(this, time, message));
        outputsAccess.readLock().unlock();
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
