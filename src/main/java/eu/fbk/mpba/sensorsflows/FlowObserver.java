package eu.fbk.mpba.sensorsflows;

/**
 * Multiple sensors call this so the sender parameter is the sender flow.
 * The receiver should implement this.
 */
interface FlowObserver {

    void onLog(Flow sensor, long time, String message);

    void onValue(Flow sensor, long time, double[] value);
}
