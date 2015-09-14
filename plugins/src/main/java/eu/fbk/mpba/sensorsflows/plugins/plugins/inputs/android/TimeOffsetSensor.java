package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.android;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Pair;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

/**
 * Nanosecond monotonic precision
 */
public class TimeOffsetSensor extends SensorComponent<Long, double[]> {

    private boolean forward = false;

    public TimeOffsetSensor(DevicePlugin<Long, double[]> parent) {
        super(parent);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        StringBuilder x = new StringBuilder(60);
        for (String i : extras.keySet())
            x.append(',')
             .append(i)
             .append("=")
             .append(extras.get(i))
             .append(']');
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()),
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
        forward = true;
        for (SensorEventEntry<Long> i : superBuffer)
            sensorEvent(i.timestamp, i.code, i.message);
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()),
                0, "Switched on");
    }

    public void computeOnEveryServer(final int passes, final LanUdpTimeClient.TimeOffsetCallback cb) {
        sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()),
                0, "Searching servers");

        LanUdpTimeClient.searchForServersAsync(new LanUdpTimeClient.ServersCallback() {
            @Override
            public void end(List<Pair<InetAddress, String>> servers) {

                StringBuilder x = new StringBuilder(60);
                x.append(", [");
                for (Pair<InetAddress, String> i : servers)
                    x.append(i.first)
                            .append("=")
                            .append(i.second)
                            .append(',');
                x.deleteCharAt(x.lastIndexOf(","));
                x.append(']');

                sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()),
                            servers.size() == 0 ? 1 : 0, "Servers found:" + servers.size() + x);

                if (servers.size() == 0) {
                    if (cb != null)
                        cb.end(true, null, "SERVER NOT FOUND", null);
                }
                else
                    for (Pair<InetAddress, String> i : servers) {
                        sensorEvent(((SmartphoneDevice) getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()),
                                2, "Computing: " + passes + " passes on " + i.first + "//" + i.second);

                        LanUdpTimeClient.computeOffsetAsync(new LanUdpTimeClient.TimeOffsetCallback() {
                                @Override
                                public void end(boolean error, InetAddress server, String serverName, LanUdpTimeClient.OffsetInfo offset) {
                                    sensorEvent(((SmartphoneDevice)getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()),
                                            error ? 4 : 3, server + ";" + serverName.replace("\\", "\\\\").replace(";", "\\,") + ";" + offset.average + ";" + offset.stDev + ";" + offset.passes);

                                    if (cb != null)
                                        cb.end(error, server, serverName, offset);
                                }
                            }, i.first, i.second, passes);
                    }
            }
        });
    }

    private List<SensorEventEntry<Long>> superBuffer = new ArrayList<>(4);

    @Override
    public void sensorEvent(Long time, int type, String message) {
        if (forward)
            super.sensorEvent(time, type, message);
        else
            superBuffer.add(new SensorEventEntry<>(this, time, type, message));
    }

    @Override
    public void switchOffAsync() {
    }

    @Override
    public String getName() {
        return LocationManager.GPS_PROVIDER;
    }
}
