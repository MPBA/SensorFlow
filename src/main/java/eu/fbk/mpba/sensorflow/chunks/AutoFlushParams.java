package eu.fbk.mpba.sensorflow.chunks;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import eu.fbk.mpba.sensorflow.Input;

class AutoFlushParams {
    private final ChunkedOutput chunkedOutput;
    private final long maxTime;
    private final int flushSize;
    private long lastFlush;
    private long start;
    private int size = 0;

    private final Timer timer = new Timer(ChunkedOutput.class.getSimpleName() + " " + AutoFlushParams.class.getSimpleName(), true);

    public AutoFlushParams(ChunkedOutput chunkedOutput, double maxTime, int maxSize) {
        this.chunkedOutput = chunkedOutput;
        this.flushSize = maxSize;
        this.maxTime = (long) (maxTime * 1_000) * 1_000_000L;
        this.lastFlush = 0;
    }

    // AnyThread
    void started(long begin) {
        start = begin;
        lastFlush = start;
        setNextFlushAlarm();
    }

    // MultiThread
    synchronized void added(int newSize, boolean last) {
        // Firstly add the amount
        size += newSize;
        // If it is time to flush
        if (last) {
            innerFlush(FlushReason.END);
            size = 0;
        } else
        if (shouldFlush()) {
            innerFlush(size >= flushSize ?
                    FlushReason.SIZE :
                    FlushReason.TIME);
            size = 0;
        }
    }

    private void innerFlush(FlushReason f) {
        // +++CS
        long now = System.nanoTime();
        chunkedOutput.flush(
                f,
                (int)((lastFlush - start) / 1000_000),
                (int)((now - start) / 1000_000));
        lastFlush = now;
        // ---CS
        setNextFlushAlarm();
    }

    private boolean shouldFlush() {
        return size >= flushSize || (size > 0 && shouldFlushByTime());
    }

    private boolean shouldFlushByTime() {
        return System.nanoTime() - lastFlush > maxTime;
    }

    /**
     * When time is up, it simply add 0 to the size to wake the situation up
     */
    private void setNextFlushAlarm() {
        // Get time in milliseconds
        Long delay = maxTime / 1_000_000L;
        // Yes, must create one every time
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // check flush by time without reading size
                if (shouldFlushByTime()) {
                    setNextFlushAlarm();
                    added(0, false);
                }
            }
        };
        // Not cancelling generates spurious wake-ups, but with newSize=0 no side effects
        timer.schedule(task, delay);
    }
}
