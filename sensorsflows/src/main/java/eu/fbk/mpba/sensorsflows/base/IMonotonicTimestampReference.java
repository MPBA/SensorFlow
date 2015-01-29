package eu.fbk.mpba.sensorsflows.base;

/**
 * Gives support to keep a monotonic timestamp reference
 */
public interface IMonotonicTimestampReference {
    void resetMonoTimestamp(long timestamp, long realTimeNanos);
    long getMonoTimestampNanos(long realTimeNanos);
}
