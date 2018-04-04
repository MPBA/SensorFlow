package eu.fbk.mpba.sensorflow.chunks;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

class AutoFlushParams {
    private final ChunkedOutput chunkedOutput;
    private final long maxTime;
    private final int flushSize;
    private long lastFlush;
    private AtomicInteger size = new AtomicInteger(0);

    private final Timer t = new Timer(ChunkedOutput.class.getSimpleName() + " " + AutoFlushParams.class.getSimpleName(), true);

    public AutoFlushParams(ChunkedOutput chunkedOutput, double maxTime, int maxSize) {
        this.chunkedOutput = chunkedOutput;
        this.flushSize = maxSize;
        this.maxTime = (long) (maxTime * 1_000) * 1_000_000L;
        this.lastFlush = System.nanoTime();
    }

    // MultiThread
    void started() {
        lastFlush = System.nanoTime();
        repost();
    }

    // OutputThread
    void added(int newSize) {
        // Firstly add the amount
        size.addAndGet(newSize);
        // If it is time to flush
        int mySize;
        while (shouldFlush(mySize = size.get())) {
            // If this thread manages to reset size,
            // it mutexes flush by size and by lastFlush in flush
            if (size.compareAndSet(mySize, 0)) {
                // +++CS
                long now = System.nanoTime();
                lastFlush = now;
                // ---CS
                chunkedOutput.flush(
                        mySize >= flushSize ?
                                FlushReason.SIZE :
                                FlushReason.TIME,
                        lastFlush,
                        now - lastFlush);
                repost();
            }
        }
    }

    private boolean shouldFlush(int mySize) {
        return mySize >= flushSize || System.nanoTime() - lastFlush > maxTime;
    }

    private void repost() {
        // Not cancelling generates spurious wake-ups, but with newSize=0 no side effects
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                added(0);
            }
        }, maxTime);
    }
}
