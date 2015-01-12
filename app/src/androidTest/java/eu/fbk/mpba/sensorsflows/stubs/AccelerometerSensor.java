package eu.fbk.mpba.sensorsflows.stubs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import eu.fbk.mpba.sensorsflows.DeviceImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * Classe di esempio.
 * Estende la classe SensorImpl che è astratta ed è parte della libreria.
 *
 * Un Sensore rappresenta una sorgente di dati simultanei ovvero di coppie tempo-insieme di valori
 * (ad esempio accelerometro &lt;time, (ax,ay,az)&gt; e giroscopio &lt;time, (gx,gy,gz)&gt; sono due
 * sensori diversi.
 *
 * Si presuppone che in questa classe vengano implementati dei metodi callback richiamati dal thread
 * del sensore reale che poi notifichino tramite i metodi
 *      public void sensorValue(TimeT time, ValueT value)
 * e
 *      public void sensorEvent(TimeT time, int type, String message)
 * protetti quindi visibili nella sottoclasse.
 */
public class AccelerometerSensor extends SensorImpl<Long, float[]> implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAcc;

    /**
     * Costruttore pienamente personalizzato
     * Io ho aggiunto un nome per debug che viene messo in toString.
     *
     * Il prima possibile va impostato il parent device del sensore con setParentDevice(...)
     */
    public AccelerometerSensor(DeviceImpl<Long, float[]> d, Context context) {
        _name = "AccelerometerSensor";
        setParentDevice(d);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAcc == null)
            changeStatus(SensorStatus.ERROR);
        else
            changeStatus(SensorStatus.OFF);
    }

    String _name;

    /**
     * Power saving
     * Nel caso il device permetta di abilitare o disabilitare alcuni sensori questi metodi verranno
     * chiamati. Sono asincroni quindi dopo l'esecuzione di questo metodo è sufficiente iniziare a
     * trasmettere dati.
     */
    @Override
    public void switchOnAsync() {
        if (getState() == SensorStatus.OFF) {
            mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);
            changeStatus(SensorStatus.ON);
            sensorEvent(System.currentTimeMillis(), 0, _name + " switched on");
        }
    }


    /**
     * Power saving
     * Nel caso il device permetta di abilitare o disabilitare alcuni sensori questi metodi verranno
     * chiamati. Sono asincroni quindi dopo l'esecuzione di questo metodo è sufficiente smettere di
     * trasmettere dati.
     */
    @Override
    public void switchOffAsync() {
        if (getState() == SensorStatus.ON) {
            mSensorManager.unregisterListener(this);
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
}
