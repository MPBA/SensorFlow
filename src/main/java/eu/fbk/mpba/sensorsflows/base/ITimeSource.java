package eu.fbk.mpba.sensorsflows.base;

/**
 * Gives support to keep a monotonic timestamp reference
 */
public interface ITimeSource {
    long getMonoUTCNanos();
    long getMonoUTCNanos(long realTimeNanos);
    long getMonoUTCMillis();
    long getMonoUTCMillis(long realTimeNanos);
}
