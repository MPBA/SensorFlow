package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * Nanosecond monotonic precision
 */
public class GpsSensor extends SensorComponent<Long, double[]> implements LocationListener {

    private LocationManager locationManager;
    private Context context;
    private long minTime;
    private float minDistance;
    private long sysToSysClockNanoOffset = 0;
    private boolean timeFallback = Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1;

    /**
     * Constructs the Listener and prepares it to be registered.
     * @param context       :   the context where to get the system service {@code Context.LOCATION_SERVICE}
     * @param minTime       :   the minimum timestamp interval for notifications,
     *                          in milliseconds. This field is only used as a hint
     *                          to conserve power, and actual timestamp between location.
     * @param minDistance   :   the minimum distance interval for notifications, in meters.
     */
    @SuppressLint("NewApi")
    public GpsSensor(NodePlugin<Long, double[]> parent, Context context, long minTime, float minDistance) {
        super(parent);
        this.minTime = minTime;
        this.minDistance = minDistance;
        this.context = context;
        if (!timeFallback)
            this.sysToSysClockNanoOffset = SystemClock.elapsedRealtimeNanos() - System.nanoTime();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    @SuppressLint("NewApi")
    public void onLocationChanged(Location location) {
        long suppNTime = timeFallback ?
                location.getTime() * 1000_000
                : (getTime().getMonoUTCNanos(location.getElapsedRealtimeNanos() - sysToSysClockNanoOffset));
        sensorValue(suppNTime,
                new double[]{
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude(),
                        location.getAccuracy()
                });
    }

    @Override
    public void onProviderDisabled(String provider) {
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                102, "disabled provider=" + provider);
        Toast.makeText(context, "Switch on the gps please", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                101, "enabled provider=" + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        StringBuilder x = new StringBuilder(60);
        for (String i : extras.keySet())
            x.append(',')
                    .append(i)
                    .append("=")
                    .append(extras.get(i))
                    .append(']');
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                status + 200, "provider=" + provider + x);
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Arrays.asList((Object) "Latitude", "Longitude", "Altitude", "Accuracy");
    }

    @Override
    public void switchOnAsync() {
        if (timeFallback)
            sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                    404, "NO_MONO_TS Could not have monotonic timestamp on the gps fixes.");
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
            changeStatus(SensorStatus.ON);
        } catch (SecurityException e) {
            sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                    403, "NO_USER_PERMIT Could not have permission from the user for fine location.");
            changeStatus(SensorStatus.ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void switchOffAsync() {
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        changeStatus(SensorStatus.OFF);
    }

    @Override
    public String getName() {
        return LocationManager.GPS_PROVIDER;
    }
}
