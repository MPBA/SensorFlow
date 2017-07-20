package eu.fbk.mpba.sensorsflows;

/**
 * Multiple sensors call this so the sender parameter is the sender flow.
 * The receiver should implement this.
 */
interface DataObserver {

    void onLog(Input sensor, long time, String message);

    void onValue(Input sensor, long time, double[] value);
}
