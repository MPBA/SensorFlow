package eu.fbk.mpba.sensorflow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class SFQueue {

    // nulls encoding
    // flows NotNull
    // null doubles AND null events --> schema-event
    // null doubles --> event
    // null events  --> values

    private final long INPUT_ADDED = 0L;
    private final long INPUT_REMOVED = -1L;

    private final Input[] flows;
    private final long[] longs;
    private final double[][] doubles;
    private final String[] strings;

    private int takeIndex;
    private int putIndex;
    private int count;

    private final Output output;

    /*
     * Concurrency control uses the classic two-condition algorithm
     * found in any textbook.
     */

    /** Main lock guarding all access */
    private final ReentrantLock lock;

    /** Condition for waiting takes */
    private final Condition notEmpty;

    /** Condition for waiting puts */
    private final Condition notFull;

    // Internal helper methods

    private void enqueued() {
        if (++putIndex == flows.length)
            putIndex = 0;
        count++;
        notEmpty.signal();
    }

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(Input f, long time, double[] v) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = time;
        doubles[putIndex] = v;
        enqueued();
    }

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(Input f, long time, String message) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = time;
        strings[putIndex] = message;
        enqueued();
    }

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(Input f, long added) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = added;
        enqueued();
    }

    private void dequeue() {
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        // cache optimization: --count == 0 --> init ==> improved locality on low queue usage
        count--;
        if (count == 0 || ++takeIndex == flows.length)
            takeIndex = 0;
        if (count == 0)
            putIndex = 0;
        notFull.signal();
    }

    SFQueue(Output drain, int capacity, boolean fair) {
        this.output = drain;
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.flows = new Input[capacity];
        this.longs = new long[capacity];
        this.doubles = new double[capacity][];
        this.strings = new String[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }


    public void put(Input f, long t, double[] v) throws InterruptedException {
        if (v == null)
            throw new NullPointerException("No support for null data");
        final ReentrantLock lock = this.lock;
//        putw = -System.nanoTime();
        lock.lockInterruptibly();
//        putw += System.nanoTime();
//        putwAvg *= .9;
//        putwAvg += .1 * putw;
        try {
            while (count == flows.length)
                notFull.await();
            enqueue(f, t, v);
//            if (System.nanoTime() - last > 100_000_000L) {
//                Manager.face.println("count:    " + count);
//                Manager.face.println("remaing:  " + (flows.length - count));
//                Manager.face.println("pollw:    " + pollw);
//                Manager.face.println("putw:     " + putw);
//                Manager.face.println("pollwAvg: " + pollwAvg);
//                Manager.face.println("putwAvg:  " + putwAvg);
//                last = System.nanoTime();
//            }
        } finally {
            lock.unlock();
        }
    }

//    long last = 0, pollw = 0, putw = 0;
//    float pollwAvg = 0, putwAvg = 0;

    public void put(Input f, long t, String v) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == flows.length)
                notFull.await();
            enqueue(f, t, v);
        } finally {
            lock.unlock();
        }
    }

    public void putAdded(Input f) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == flows.length)
                notFull.await();
            enqueue(f, INPUT_ADDED);
        } finally {
            lock.unlock();
        }
    }

    public void putRemoved(Input f) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == flows.length)
                notFull.await();
            enqueue(f, INPUT_REMOVED);
        } finally {
            lock.unlock();
        }
    }

    public void pollToHandler(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        Input f;
        long t;
        boolean isVal;
        boolean isLog;
        double[] d = null;
        String s = null;
//        pollw = -System.nanoTime();
        lock.lockInterruptibly();
//        pollw += System.nanoTime();
//        pollwAvg *= .9;
//        pollwAvg += .1 * pollw;
        try {
            while (count == 0) {
                if (nanos > 0) {
                    nanos = notEmpty.awaitNanos(nanos);
                } else {
                    return;
                }
            }
            f = flows[takeIndex];
            flows[takeIndex] = null;
            t = longs[takeIndex];
            isVal = doubles[takeIndex] != null;
            isLog = strings[takeIndex] != null;
            if (isVal) {
                // Value
                d = doubles[takeIndex];
                doubles[takeIndex] = null;
            }
            if (isLog) {
                // Log
                s = strings[takeIndex];
                strings[takeIndex] = null;
            }
            dequeue();
        } finally {
            lock.unlock();
        }
        if (isVal) {
            // Is Value
            output.onValue(f, t, d);
        } else if (isLog) {
            // Is Log
            output.onLog(f, t, s);
        } else {
            // Is Schema Event
            if (t == INPUT_ADDED)
                output.onInputAdded(f);
            else
                output.onInputRemoved(f);
        }
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return flows.length - count;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k = count;
            if (k > 0) {
                takeIndex = putIndex;
                count = 0;
                for (; k > 0 && lock.hasWaiters(notFull); k--)
                    notFull.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
