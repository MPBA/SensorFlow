package eu.fbk.mpba.sensorsflows.base;

/**
 * Gives support to keep a monotonic timestamp reference
 */
public interface IMonotonicTimestampReference {
    // TODO: improve the structure.
    // Here it is needed only a get, a reset can be internal
    // or alternatively the get(void) and the reset or a get reference/bootTime
    void resetMonoTimestamp(long timestamp, long realTimeNanos);
    long getMonoTimestampNanos(long realTimeNanos);
}
