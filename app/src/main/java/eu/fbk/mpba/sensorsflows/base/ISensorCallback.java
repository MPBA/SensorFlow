package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for a sensor's data and state callback.
 * Multiple sensors call this so the sender parameter is the sender sensor.
 * The receiver should implement this.
 */
public interface ISensorCallback<SensorT, TimeT, ValueT> {

    void pushData(SensorT sensor, TimeT time, ValueT value);

    void sensorEvent(SensorT sensor, int type, String message);

    void sensorStateChanged(SensorT sensor, SensorStatus state);
}
