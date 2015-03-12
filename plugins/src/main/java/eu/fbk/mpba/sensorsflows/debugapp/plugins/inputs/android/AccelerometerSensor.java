package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * Nanosecond monotonic precision
 */
public class AccelerometerSensor extends SensorComponent<Long, double[]> implements SensorEventListener {

    private SensorManager _sensorMan;
    private Sensor _sAcc;
    private String _name;
    private int _delay;

    public AccelerometerSensor(DevicePlugin<Long, double[]> d, Context context, String name, int sensorDelay) {
        super(d);
        _delay = sensorDelay;
        _name = name;
        _sensorMan = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        _sAcc = _sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void switchOnAsync() {
        if (_sAcc == null)
            changeStatus(SensorStatus.ERROR);
        else if (getState() == SensorStatus.OFF) {
            _sensorMan.registerListener(this, _sAcc, _delay);
            changeStatus(SensorStatus.ON);
            sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                    0, "Switched on");
        }
    }

    @Override
    public void switchOffAsync() {
        if (getState() == SensorStatus.ON) {
            _sensorMan.unregisterListener(this);
            changeStatus(SensorStatus.OFF);
            sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                    0, "Switched off");
        }
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link android.hardware.SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link android.hardware.SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorValue(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(event.timestamp),
                new double[] {event.values[0], event.values[1], event.values[2]});
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link android.hardware.SensorManager SensorManager} for details.
     *
     * @param sensor Sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                accuracy, "Accuracy changed");
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object)"x0", "x1", "x2");
    }

    @Override
    public String toString() {
        return (getParentDevicePlugin() != null ? getParentDevicePlugin().toString() + "/" : "") + _sAcc.getName() + "-" + _name;
    }
}
