package eu.fbk.mpba.sensorflow;

public interface TimeSource {
    long getMonoUTCNanos();
    long getMonoUTCNanos(long realTimeNanos);
}
