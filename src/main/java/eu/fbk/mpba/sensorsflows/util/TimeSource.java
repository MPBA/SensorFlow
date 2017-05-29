package eu.fbk.mpba.sensorsflows.util;

/**
 * Gives support to keep a monotonic timestamp reference
 */
public interface TimeSource {
    long getMonoUTCNanos();
    long getMonoUTCNanos(long realTimeNanos);
    long getMonoUTCMillis();
    long getMonoUTCMillis(long realTimeNanos);
}
