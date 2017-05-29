package eu.fbk.mpba.sensorsflows;

/**
 * Main interface for a flow's data and state callback.
 * Multiple sensors call this so the sender parameter is the sender flow.
 * The receiver should implement this.
 */
interface FlowObserver {

    void onStatusChanged(Flow sensor, long time, Flow.Status state);

    void onEvent(Flow sensor, long time, int type, String message);

    void onValue(Flow sensor, long time, double[] value);
}
