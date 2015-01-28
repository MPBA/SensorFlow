package eu.fbk.mpba.sensorsflows.gpspluginapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.SensorComponent;

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
    public GpsSensor(Context context, String name, long minTime, float minDistance) {
        this.minTime = minTime;
        this.minDistance = minDistance;
        this.context = context;
        this.name = name;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(Location location) {
        String str = "Lat: "+location.getLatitude()+"\nLon: "+location.getLongitude();
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
        sensorValue(location.getElapsedRealtimeNanos(),
                new double[] {
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude(),
                        location.getBearing(),
                        location.getAccuracy(),
                        location.getTime()
                });
    }

    @Override
    public void onProviderDisabled(String provider) {
        sensorEvent(System.nanoTime(), -2, provider + "\\\\" + ":m Gps provider disabled");
        Toast.makeText(context, "Gps turned off ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        sensorEvent(System.nanoTime(), -1, provider + "\\\\" + ":m Gps provider enabled");
        Toast.makeText(context, "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        sensorEvent(System.nanoTime(), status, provider + "\\\\" + extras.toString());
        Toast.makeText(context, "Gps StatusChanged " + status, Toast.LENGTH_LONG).show();
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object)
                "android.location.Location#getLatitude",
                "android.location.Location#getLongitude",
                "android.location.Location#getAltitude",
                "android.location.Location#getBearing",
                "android.location.Location#getAccuracy",
                "android.location.Location#getTime");
    }

    @Override
    public String toString() {
        return getParentDevice().toString() + "/" + LocationManager.GPS_PROVIDER + "-" + name;
    }

    @Override
    public void switchOnAsync() {
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, minTime, minDistance, this);
    }

    @Override
    public void switchOffAsync() {
        locationManager.removeUpdates(this);
    }
}
