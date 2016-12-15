package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for a flow's data and state callback.
 * Multiple sensors call this so the sender parameter is the sender flow.
 * The receiver should implement this.
 */
public interface IFlowCallback<SensorT extends ISensor> {

    void onStatusChanged(SensorT sensor, long time, SensorStatus state);

    void onEvent(SensorT sensor, long time, int type, String message);

    void onValue(SensorT sensor, long time, double[] value);
}
