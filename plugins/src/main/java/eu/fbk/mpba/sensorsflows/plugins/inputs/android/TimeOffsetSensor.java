package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

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
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                status + 200, "provider=" + provider + x);
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Collections.emptyList();
    }

    @Override
    public void switchOnAsync() {
        forward = true;
        for (SensorEventEntry<Long> i : superBuffer)
            sensorEvent(i.timestamp, i.code, i.message);
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                0, "Switched on");
    }

    public void computeOnEveryServer(final int passes, final LanUdpTimeClient.TimeOffsetCallback cb) {
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                0, "Searching servers");

        LanUdpTimeClient.searchForServersAsync(new LanUdpTimeClient.ServersCallback() {
            @Override
            public void end(List<Pair<InetAddress, String>> servers, boolean networkUnreachable) {

                StringBuilder x = new StringBuilder(60);
                x.append(", [");
                for (Pair<InetAddress, String> i : servers)
                    x.append(i.first)
                            .append("=")
                            .append(i.second)
                            .append(',');
                x.deleteCharAt(x.lastIndexOf(","));
                x.append(']');

                sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                        servers.size() == 0 ? 1 : 0, "Servers found:" + servers.size() + x);

                if (servers.size() == 0) {
                    if (cb != null)
                        cb.end(true, null, networkUnreachable ? "No connection" : "SERVER NOT FOUND", null);
                } else
                    for (Pair<InetAddress, String> i : servers) {
                        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                                2, "Computing: " + passes + " passes on " + i.first + " " + i.second);
                        final Semaphore c = new Semaphore(0);
                        LanUdpTimeClient.computeOffsetAsync(new LanUdpTimeClient.TimeOffsetCallback() {
                            @Override
                            public void end(boolean error, InetAddress server, String serverName, LanUdpTimeClient.OffsetInfo offset) {
                                sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                                        error ? 4 : 3, server + ";" + serverName.replace("\\", "\\\\").replace(";", "\\,") + ";" + offset.average + ";" + offset.stDev + ";" + offset.passes);
                                c.release();
                                if (cb != null)
                                    cb.end(error, server, serverName, offset);
                            }
                        }, i.first, i.second, passes, getTime().getMonoUTCNanos(0));
                        try {
                            c.acquire();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
            }
        });
    }

    private List<SensorEventEntry<Long>> superBuffer = new ArrayList<>(4);

    @Override
    public void sensorEvent(Long time, int type, String message) {
        Log.d("TIME", "e:" + type + " -> " + message);
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

    public void clear() {
        superBuffer.clear();
    }

    private LanUdpTimeServer s;

    public void startTimeServer() {
        if (s == null) {
            s = new LanUdpTimeServer();
            s.BeginListening();
        }
    }

    public void stopTimeServer() {
        if (s != null) {
            s.StopListening();
            s = null;
        }
    }

    public boolean isTimeServerRunning() {
        return s != null;
    }
}
