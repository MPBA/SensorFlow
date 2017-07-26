package eu.fbk.mpba.sensorflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Input implements InputGroup {

    //      Static time - things

    private static long bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();
    private static TimeSource time = new TimeSource() {

        @Override
        public long getMonoUTCNanos() {
            return System.nanoTime() + bootTime;
        }

        @Override
        public long getMonoUTCNanos(long systemNanoTime) {
            return systemNanoTime + bootTime;
        }
    };

    //      Fields

    private boolean listened = true;
    private String name;

    private InputGroup parent;
    private Collection<String> header;
    private Set<OutputManager> outputs = new HashSet<>();
    private ReentrantReadWriteLock outputsAccess = new ReentrantReadWriteLock(false);

    //      Constructors

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

    //      Outputs access

    void addOutput(OutputManager output) {
        outputsAccess.writeLock().lock();
        outputs.add(output);
        outputsAccess.writeLock().unlock();
        onOutputsAdded();
    }

    void removeOutput(OutputManager output) {
        outputsAccess.writeLock().lock();
        outputs.remove(output);
        outputsAccess.writeLock().unlock();
    }

    void addOutput(Collection<OutputManager> output) {
        outputsAccess.writeLock().lock();
        outputs.addAll(output);
        outputsAccess.writeLock().unlock();
        onOutputsAdded();
    }

    void removeOutput(Collection<OutputManager> output) {
        outputsAccess.writeLock().lock();
        outputs.removeAll(output);
        outputsAccess.writeLock().unlock();
    }

    Collection<OutputManager> getOutputs() {
        outputsAccess.readLock().lock();
        ArrayList<OutputManager> outputManagers = new ArrayList<>(outputs);
        outputsAccess.readLock().unlock();
        return outputManagers;
    }

    //      Outputs access - Notify

    public void pushValue(long time, double[] value) {
        // Shouldn't be called before onCreateAndStart
        outputsAccess.readLock().lock();
        outputs.stream()
                .filter(OutputManager::isEnabled)
                .forEach(o -> o.pushValue(this, time, value));
        outputsAccess.readLock().unlock();

    }

    public void pushLog(long time, String message) {
        // Shouldn't be called before onCreateAndStart
        outputsAccess.readLock().lock();
        outputs.stream()
                .filter(OutputManager::isEnabled)
                .forEach(o -> o.pushLog(this, time, message));
        outputsAccess.readLock().unlock();
    }

    //      Muting

    public boolean isMuted() {
        return !listened;
    }

    public void mute() {
        this.listened = false;
    }

    public void unmute() {
        this.listened = true;
    }

    //      Gets

    public static TimeSource getTimeSource() {
        return time;
    }

    public InputGroup getParent() {
        return parent;
    }

    public final Collection<String> getHeader(){
        return header;
    }

    //      Gets - NamedPlugin non-final Overrides

    @Override
    public String getName() {
        InputGroup parent = getParent();
        return parent != null ? parent.getName() + "/" + getSimpleName() : getSimpleName();
    }

    //      Gets - InputGroup final Overrides

    @Override
    public final String getSimpleName() {
        return name;
    }

    @Override
    public final Iterable<Input> getChildren() {
        return Collections.singletonList(this);
    }

    //      Extra events

    public void onOutputsAdded() { }

    //      Finalization

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

    //      Deprecated

    @Deprecated
    protected Status status = Status.OFF;

    @Deprecated
    protected void changeStatus(Status state) {
        // Not notified to SF
        status = state;
    }

    @Deprecated
    public Status getStatus() {
        return status;
    }

    @Deprecated
    public enum Status {
        OFF, ON, ERROR
    }
}