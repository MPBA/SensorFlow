package eu.fbk.mpba.sensorsflows.stubs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DeviceImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

public class AccelerometerSensor extends SensorImpl<Long, float[]> implements SensorEventListener {

    private SensorManager _sensorMan;
    private Sensor _sAcc;
    private String _name;

    public AccelerometerSensor(DeviceImpl<Long, float[]> d, Context context) {
        _name = "AccelerometerSensor";
        setParentDevice(d);

        _sensorMan = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        _sAcc = _sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (_sAcc == null)
            changeStatus(SensorStatus.ERROR);
        else
            changeStatus(SensorStatus.OFF);
    }

    @Override
    public void switchOnAsync() {
        if (getState() == SensorStatus.OFF) {
            _sensorMan.registerListener(this, _sAcc, SensorManager.SENSOR_DELAY_NORMAL);
            changeStatus(SensorStatus.ON);
            sensorEvent(System.currentTimeMillis(), 0, _name + " switched on");
        }
    }

    @Override
    public void switchOffAsync() {
        if (getState() == SensorStatus.ON) {
            _sensorMan.unregisterListener(this);
            changeStatus(SensorStatus.OFF);
            sensorEvent(System.currentTimeMillis(), 0, _name + " switched off");
        }
    }

    @Override
    public String toString() {
        return "Sensor:" + _name;
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
        sensorValue(event.timestamp, event.values);
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
        sensorEvent(System.currentTimeMillis(), accuracy, "Accuracy changed");
    }

    @Override
    public List<String> getValuesDescriptors() {
        return Arrays.asList("AccX", "AccY", "AccZ");
    }
}
