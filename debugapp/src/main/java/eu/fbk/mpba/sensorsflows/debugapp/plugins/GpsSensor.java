package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

    /**
     * Constructs the Listener and prepares it to be registered.
     * @param context       :   the context where to get the system service {@code Context.LOCATION_SERVICE}
     * @param minTime       :   the minimum time interval for notifications,
     *                          in milliseconds. This field is only used as a hint
     *                          to conserve power, and actual time between location.
     * @param minDistance   :   the minimum distance interval for notifications, in meters.
     */
    public GpsSensor(DevicePlugin<Long, double[]> parent, Context context, String name, long minTime, float minDistance) {
        super(parent);
        this.minTime = minTime;
        this.minDistance = minDistance;
        this.context = context;
        this.name = name;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(Location location) {
        sensorValue(((SmartphoneDevice) getParentDevicePlugin()).getMonoTimestampNanos(location.getElapsedRealtimeNanos()),
                new double[]{
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude(),
                        location.getBearing(),
                        location.getAccuracy()
                });
    }

    @Override
    public void onProviderDisabled(String provider) {
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                -2, provider + "\\\\" + "provider disabled");
        Toast.makeText(context, "Switch on the gps please", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        sensorEvent(((SmartphoneDevice) getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                -1, provider + "\\\\" + "provider enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        StringBuilder x = new StringBuilder(60);
        for (String i : extras.keySet())
            x.append('[')
             .append(i)
             .append(", ")
             .append(extras.get(i))
             .append(']');
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                status, provider + "\\\\" + x);
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object)
                "Latitude",
                "Longitude",
                "Altitude",
                "Bearing",
                "Accuracy");
    }

    @Override
    public void switchOnAsync() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        changeStatus(SensorStatus.ON);
    }

    @Override
    public void switchOffAsync() {
        locationManager.removeUpdates(this);
        changeStatus(SensorStatus.OFF);
    }

    @Override
    public String toString() {
        return (getParentDevicePlugin() != null ? getParentDevicePlugin().toString() + "/" : "") + LocationManager.GPS_PROVIDER + "-" + name;
    }
}
