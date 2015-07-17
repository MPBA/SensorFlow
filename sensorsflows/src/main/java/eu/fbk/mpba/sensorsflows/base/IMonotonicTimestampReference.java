package eu.fbk.mpba.sensorsflows.base;

/**
 * Gives support to keep a monotonic timestamp reference
 */
public interface IMonotonicTimestampReference {
    long getMonoUTCNanos(long realTimeNanos);
}
