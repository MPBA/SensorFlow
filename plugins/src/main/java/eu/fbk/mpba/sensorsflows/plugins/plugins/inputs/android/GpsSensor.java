package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.android;

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

import eu.fbk.mpba.sensorsflows.DevicePlugin;
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
    private String name;
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
    public GpsSensor(DevicePlugin<Long, double[]> parent, Context context, String name, long minTime, float minDistance) {
        super(parent);
        this.minTime = minTime;
        this.minDistance = minDistance;
        this.context = context;
        this.name = name;
        if (!timeFallback)
            this.sysToSysClockNanoOffset = SystemClock.elapsedRealtimeNanos() - System.nanoTime();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override @SuppressLint("NewApi")
    public void onLocationChanged(Location location) {
        long suppNTime = timeFallback ?
                location.getTime() * 1000_000
            :   ((SmartphoneDevice) getParentDevicePlugin()).getMonoTimestampNanos(location.getElapsedRealtimeNanos() - sysToSysClockNanoOffset);
        sensorValue(suppNTime,
                new double[]{
                        location.getLongitude(),
                        location.getLatitude(),
                        location.getAltitude(),
                        location.getAccuracy()
                });
    }

    @Override
    public void onProviderDisabled(String provider) {
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                102, "disabled provider=" + provider);
        Toast.makeText(context, "Switch on the gps please", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        sensorEvent(((SmartphoneDevice) getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
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
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                status + 200, "provider=" + provider +  x);
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object)
                "Longitude",
                "Latitude",
                "Altitude",
                "Accuracy");
    }

    @Override
    public void switchOnAsync() {
        if (timeFallback)
            sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                    404, "NO_MONO_TS Could not have monotonic timestamp on the gps fixes.");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        changeStatus(SensorStatus.ON);
    }

    @Override
    public void switchOffAsync() {
        locationManager.removeUpdates(this);
        changeStatus(SensorStatus.OFF);
    }

    @Override
    public String getName() {
        return (getParentDevicePlugin() != null ? getParentDevicePlugin().toString() + "/" : "") + LocationManager.GPS_PROVIDER + "-" + name;
    }
}
