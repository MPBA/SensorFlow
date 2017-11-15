package eu.fbk.mpba.sensorflow.chunks;

import java.util.Timer;
import java.util.TimerTask;

class AutoFlushParams {
    private ChunkedOutput chunkedOutput;
    private final long maxTime;
    private final int flushSize;
    private long lastFlush;
    private int size = 0;

    private Timer t = new Timer(ChunkedOutput.class.getSimpleName() + " " + AutoFlushParams.class.getSimpleName(), true);

    public AutoFlushParams(ChunkedOutput chunkedOutput, double maxTime, int maxSize) {
        this.chunkedOutput = chunkedOutput;
        this.flushSize = maxSize;
        this.maxTime = (long) (maxTime * 1_000) * 1_000_000L;
        this.lastFlush = 0;
    }

    // MultiThread
    void started() {
        lastFlush = System.nanoTime();
        repost();
    }

    // OutputThread
    void added(int newSize) {
        size += newSize;
        if (size >= flushSize || System.nanoTime() - lastFlush > maxTime) {
            chunkedOutput.flush(size >= flushSize ? FlushReason.SIZE : FlushReason.TIME);
            lastFlush = System.nanoTime();
            size = 0;
            repost();
        }
    }

    private void repost() {
        // Not cancelling generates spurious wake-ups
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (chunkedOutput) {
                    added(0);
                }
            }
        }, maxTime);
    }
}
