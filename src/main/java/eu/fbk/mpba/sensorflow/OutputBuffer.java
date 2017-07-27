package eu.fbk.mpba.sensorflow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class OutputBuffer implements Output {

    /*
        nulls encoding
        flows NotNull
        null doubles AND null events --> schema-event
        null doubles --> event
        null events  --> values
    */

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

    private final ReentrantLock lock;

    private final Condition notEmpty;

    private final Condition notFull;

    //      Internal helper methods

    private void enqueued() {
        if (++putIndex == flows.length)
            putIndex = 0;
        count++;
        notEmpty.signal();
    }

    private void enqueue(Input f, long time, double[] v) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = time;
        doubles[putIndex] = v;
        enqueued();
    }

    private void enqueue(Input f, long time, String message) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        flows[putIndex] = f;
        longs[putIndex] = time;
        strings[putIndex] = message;
        enqueued();
    }

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

    OutputBuffer(Output drain, int capacity, boolean fair) {
        if (drain == null)
            throw new NullPointerException();
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.output = drain;
        this.flows = new Input[capacity];
        this.longs = new long[capacity];
        this.doubles = new double[capacity][];
        this.strings = new String[capacity];
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    //      Output interface

    @Override
    public void onValue(Input f, long t, double[] v) {
        if (v == null)
            throw new NullPointerException("No support for null data");
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            try {
                while (count == flows.length)
                    notFull.await();
                enqueue(f, t, v);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLog(Input f, long t, String v) {
        if (v == null)
            throw new IllegalArgumentException("No support for null logs");

        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            try {
                while (count == flows.length)
                    notFull.await();
                enqueue(f, t, v);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInputAdded(Input f) {
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            try {
                while (count == flows.length)
                    notFull.await();
                enqueue(f, INPUT_ADDED);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInputRemoved(Input f) {
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
            try {
                while (count == flows.length)
                    notFull.await();
                enqueue(f, INPUT_REMOVED);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return output.getName();
    }

    @Override
    public void onCreate(String sessionId) {
        output.onCreate(sessionId);
    }

    @Override
    public void onClose() {
        output.onClose();
    }

    //      Flushing

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

    //      Control

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

    //      Gets

    public Output getHandler() {
        return output;
    }
}
