package eu.fbk.mpba.sensorflow.chunks;

import java.util.Date;
import java.util.TreeMap;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.SingleThreadRequired;
import eu.fbk.mpba.sensorflow.sense.OutputModule;

@SingleThreadRequired
public class ChunksOutput extends OutputModule {

    private final AutoFlushParams autoFlushParams;
    private final TreeMap<Integer, Input> inputInfo = new TreeMap<>();
    private final ChunkCooker.Factory factory;
    private volatile ChunkCooker chunk;
    private Consumer consumer;
    private String sessionId;
    private String trackName;
    private int splits;

    public interface Consumer {
        void start(String sessionId, String trackName, Date beginTime);
        void next(ChunkCooker chunk);
        void stop();
    }

    // Access AnyThread (atomic ctor)
    public ChunksOutput(String name, double maxChunkDuration, int maxChunkBytes, ChunkCooker.Factory factory) {
        super(name, "");
        this.factory = factory;
        autoFlushParams = new AutoFlushParams(this, maxChunkDuration, maxChunkBytes);
    }

    private synchronized int nextSplitID() {
        return splits++;
    }

    public synchronized void startRecording(Consumer consumer, String trackName) {
        if (consumer == null)
            throw new NullPointerException("Consumer is null.");
        if (this.consumer != null)
            throw new UnsupportedOperationException("Already registered a Consumer with trackName " + this.trackName);

        this.consumer = consumer;   // Started condition true
        this.trackName = trackName;

        long begin = System.nanoTime();
        Date beginTime = new Date(Input.getTimeSource().getMonoUTCNanos(begin) / 1000_000);

        consumer.start(sessionId, trackName, beginTime);
        ChunkCooker firstChunk = factory.newInstance();
        firstChunk.setTrackName(trackName);

        for (Input i : inputInfo.values())
            firstChunk.addInput(i);
        chunk = firstChunk;
        autoFlushParams.started(begin);
    }

    public synchronized void checkFlush() {
        autoFlushParams.added(0, false);
    }

    public synchronized void stopRecording() {
        if (consumer != null) {
            autoFlushParams.added(0, true);
            consumer.stop();
            chunk = null;
            consumer = null;            // Started condition false
        }
    }

    synchronized void flush(FlushReason r, int begin, int end) {
        chunk.setId(nextSplitID());
        chunk.setFlushReason(r);
        chunk.setBegin(begin);
        chunk.setDuration(end - begin);
        consumer.next(chunk);
        chunk = factory.newInstance();
        chunk.setTrackName(trackName);
    }

    // OutputPlugIn implementation

    public synchronized void onCreate(String id) {
        sessionId = id;
        splits = 0;
    }

    public synchronized void onInputAdded(Input input) {
        inputInfo.put(input.intUid, input);
        if (chunk != null)
            chunk.addInput(input);
    }

    public synchronized void onInputRemoved(Input input) {
        inputInfo.remove(input.intUid);
    }

    public synchronized void onLog(Input flow, long time, String message) {
        if (chunk == null)
            return;

        // Approx message length
        autoFlushParams.added(chunk.addLog(flow, time, message), false);
    }

    public synchronized void onValue(Input flow, long time, double[] value) {
        if (chunk == null)
            return;

        autoFlushParams.added(chunk.addValue(flow, time, value), false);
    }

    public void onClose() {
        stopRecording();
    }

    // Never called
    public void onLog(Input input, long timestamp, int type, String tag, String message) {  }
}
