package eu.fbk.mpba.sensorflow;

/**
 * Gives support to keep a monotonic timestamp reference
 */
public interface TimeSource {
    long getMonoUTCNanos();
    long getMonoUTCNanos(long realTimeNanos);
}
