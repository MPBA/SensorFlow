package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface for a sensor's data and state callback.
 * Multiple sensors call this so the sender parameter is the sender sensor.
 * The receiver should implement this.
 */
public interface ISensorCallback<SensorT extends ISensor, TimeT, ValueT> {

    void sensorStateChanged(SensorT sensor, TimeT time, SensorStatus state);

    void sensorEvent(SensorT sensor, TimeT time, int type, String message);

    void offerData(SensorT sensor, TimeT time, ValueT value);
}
