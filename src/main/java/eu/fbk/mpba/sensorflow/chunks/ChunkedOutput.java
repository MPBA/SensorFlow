package eu.fbk.mpba.sensorflow.chunks;

import java.util.TreeMap;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.SingleThreadRequired;
import eu.fbk.mpba.sensorflow.sense.OutputModule;

@SingleThreadRequired
public class ChunkedOutput extends OutputModule {

    private final AutoFlushParams autoFlushParams;
    private final TreeMap<Integer, Input> inputInfo = new TreeMap<>();
    private final ChunkCooker.Factory factory;
    private volatile ChunkCooker chunk;
    private Consumer consumer;
    private String sessionId;
    private String trackName;
    private int splits;

    public interface Consumer {
        void start(String sessionId, String trackName);
        void next(ChunkCooker chunk);
        void stop();
    }

    private int nextSplitID() {
        return splits++;
    }

    // Access AnyThread (atomic ctor)
    public ChunkedOutput(String name, double maxChunkDuration, int maxChunkBytes, ChunkCooker.Factory factory) {
        super(name, "");
        this.factory = factory;
        autoFlushParams = new AutoFlushParams(this, maxChunkDuration, maxChunkBytes);
    }

    // Access UserFGThread only
    public void startRecording(Consumer consumer, String trackName) {
        if (consumer == null)
            throw new NullPointerException("Consumer is null.");
        if (this.consumer != null)
            throw new UnsupportedOperationException("Already registered a Consumer with trackName " + this.trackName);

        this.consumer = consumer;   // Started condition true
        this.trackName = trackName;

        consumer.start(sessionId, trackName);
        ChunkCooker firstChunk = factory.newInstance();
        firstChunk.setTrackName(trackName);
        synchronized (this) {
            for (Input i : inputInfo.values())
                firstChunk.addInput(i);
            chunk = firstChunk;
            autoFlushParams.started();
        }
    }

    // Access UserFGThread only
    public synchronized void stopRecording() {
        if (consumer != null) {
            autoFlushParams.lastFlush();
            consumer.stop();
            chunk = null;
            consumer = null;            // Started condition false
        }
    }

    // Access OutputThread or synced UIThread only
    synchronized void flush(FlushReason r, long begin, long duration) {
        chunk.setId(nextSplitID());
        chunk.setFlushReason(r);
        chunk.setBegin((int)(Input.getTimeSource().getMonoUTCNanos(begin) / 1000_000));
        chunk.setDuration((int)(duration / 1000_000));
        consumer.next(chunk);
        chunk = factory.newInstance();
        chunk.setTrackName(trackName);
    }

    // OutputPlugIn implementation

    // Access UserFGThread only
    public void onCreate(String id) {
        sessionId = id;
        splits = 0;
    }

    // Access OutputThread only
    public synchronized void onInputAdded(Input input) {
        inputInfo.put(input.intUid, input);
        if (chunk != null)
            chunk.addInput(input);
    }

    // Access OutputThread only
    public synchronized void onInputRemoved(Input input) {
        inputInfo.remove(input.intUid);
    }

    // Access OutputThread only
    public synchronized void onLog(Input flow, long time, String message) {
        if (chunk == null)
            return;

        // Approx message length
        autoFlushParams.added(chunk.addLog(flow, time, message));
    }

    // Access OutputThread only
    public synchronized void onValue(Input flow, long time, double[] value) {
        if (chunk == null)
            return;

        autoFlushParams.added(chunk.addValue(flow, time, value));
    }

    // Access AnyThread
    public synchronized void onClose() {
        stopRecording();
    }

    // Never called
    public void onLog(Input input, long timestamp, int type, String tag, String message) {  }
}
