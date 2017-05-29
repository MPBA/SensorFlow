package eu.fbk.mpba.sensorsflows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class FlowBuffer {

    private final Flow[] flows;
    private final long[] longs;
    private final double[][] doubles;
    private final int[] ints;
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

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(Flow f, long time, double[] v) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = time;
        doubles[putIndex] = v;
        if (++putIndex == flows.length)
            putIndex = 0;
        count++;
        notEmpty.signal();
    }

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(Flow f, long time, int code, String message) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = time;
        ints[putIndex] = code;
        strings[putIndex] = message;
        if (++putIndex == flows.length)
            putIndex = 0;
        count++;
        notEmpty.signal();
    }

    private void dequeue() {
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        if (++takeIndex == flows.length)
            takeIndex = 0;
        count--;
        notFull.signal();
    }

    FlowBuffer(Output drain, int capacity, boolean fair) {
        this.output = drain;
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.flows = new Flow[capacity];
        this.longs = new long[capacity];
        this.doubles = new double[capacity][];
        this.strings = new String[capacity];
        this.ints = new int[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }


    public void put(Flow f, long t, double[] v) throws InterruptedException {
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
//                SensorFlow.face.println("count:    " + count);
//                SensorFlow.face.println("remaing:  " + (flows.length - count));
//                SensorFlow.face.println("pollw:    " + pollw);
//                SensorFlow.face.println("putw:     " + putw);
//                SensorFlow.face.println("pollwAvg: " + pollwAvg);
//                SensorFlow.face.println("putwAvg:  " + putwAvg);
//                last = System.nanoTime();
//            }
        } finally {
            lock.unlock();
        }
    }

//    long last = 0, pollw = 0, putw = 0;
//    float pollwAvg = 0, putwAvg = 0;

    public void put(Flow f, long t, int c, String v) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == flows.length)
                notFull.await();
            enqueue(f, t, c, v);
        } finally {
            lock.unlock();
        }
    }

    public void poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        Flow f;
        long t;
        boolean dataIs;
        double[] d = null;
        int c = 0;
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
            t = longs[takeIndex];
            dataIs = doubles[takeIndex] == null;
            if (dataIs) {
                d = doubles[takeIndex];
                doubles[takeIndex] = null;
            }
            else {
                c = ints[takeIndex];
                s = strings[takeIndex];
                strings[takeIndex] = null;
            }
            dequeue();
        } finally {
            lock.unlock();
        }
        if (dataIs) {
            output.onValue(f, t, d);
        }
        else {
            output.onLog(f, t, c, s);
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
