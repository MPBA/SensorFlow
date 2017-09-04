package eu.fbk.mpba.sensorflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
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

    private static AtomicLong sequential = new AtomicLong(1L);

    //      Fields

    public final int intUid;
    private volatile boolean listened = true;
    private String name;

    private InputGroup parent;
    private List<String> header;
    private Set<OutputManager> outputs = new HashSet<>();
    private ReentrantReadWriteLock outputsAccess = new ReentrantReadWriteLock(false);
    private TreeMap<String, String> dictionary = new TreeMap<>();
    private ReentrantReadWriteLock dictionaryAccess = new ReentrantReadWriteLock(false);

    //      Constructors

    protected Input(Collection<String> header) {
        this(null, Input.class.getSimpleName(), header);
    }

    protected Input(InputGroup parent, Collection<String> header) {
        this(parent, Input.class.getSimpleName(), header);
    }

    protected Input(InputGroup parent, String name, Collection<String> header) {
        long longUid = sequential.getAndIncrement();
        this.intUid = (int) longUid / 2 * ((int) longUid % 2 * 2 - 1);
        this.parent = parent;
        this.name = name != null ? name : getClass().getSimpleName() + "-" + hashCode();
        this.header = new ArrayList<>(header);
    }

    //      Outputs access

    void addOutput(OutputManager output) {
        outputsAccess.writeLock().lock();
        outputs.add(output);
        outputsAccess.writeLock().unlock();
        if (output.getStatus() == PluginStatus.ADDED) {
            pushDictionary(output);
        }
    }

    void removeOutput(OutputManager output) {
        outputsAccess.writeLock().lock();
        outputs.remove(output);
        outputsAccess.writeLock().unlock();
    }

    void pushDictionary(OutputManager output) {
        dictionaryAccess.readLock().lock();
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            output.pushLog(this, getTimeSource().getMonoUTCNanos(), formatKeyValue(entry.getKey(), entry.getValue()));
        }
        dictionaryAccess.readLock().unlock();
    }

//    void addOutput(Collection<OutputManager> output) {
//        outputsAccess.writeLock().lock();
//        outputs.addAll(output);
//        outputsAccess.writeLock().unlock();
//        onOutputAdded();
//    }
//
//    void removeOutput(Collection<OutputManager> output) {
//        outputsAccess.writeLock().lock();
//        outputs.removeAll(output);
//        outputsAccess.writeLock().unlock();
//    }

    Collection<OutputManager> getOutputs() {
        outputsAccess.readLock().lock();
        ArrayList<OutputManager> outputManagers = new ArrayList<>(outputs);
        outputsAccess.readLock().unlock();
        return outputManagers;
    }

    //      Outputs access - Notify

    private String formatKeyValue(String key, String value) {
        String separator = ",";
        final String key2 = key.replace("\\", "\\\\").replace(separator, "\\s");
        final String value2 = value.replace("\\", "\\\\").replace(separator, "\\s");
        return key2 + separator + value2;
    }

    public void putKeyValue(String key, String value) {
        dictionaryAccess.writeLock().lock();
        String old = dictionary.put(key, value);
        dictionaryAccess.writeLock().unlock();
        if (listened && !value.equals(old)) {
            pushLog(getTimeSource().getMonoUTCNanos(), formatKeyValue(key, value));
        }
    }

    public void pushValue(long time, double value) {
        pushValue(time, new double[] { value });
    }

    public void pushValue(long time, double[] value) {
        // Shouldn't be called before onCreateAndAdded
        if (listened) {
            outputsAccess.readLock().lock();
            for (OutputManager output : outputs)
                if (output.isEnabled())
                    output.pushValue(this, time, value);
            outputsAccess.readLock().unlock();
        }
    }

    public void pushLog(long time, String message) {
        // Shouldn't be called before onCreateAndAdded
        if (listened) {
            outputsAccess.readLock().lock();
            for (OutputManager output : outputs)
                if (output.isEnabled())
                    output.pushLog(this, time, message);
            outputsAccess.readLock().unlock();
        }
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

    public final List<String> getHeader(){
        return header;
    }

    //      Gets - SFPlugin non-final Overrides

    @Override
    public final String getName() {
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
}
