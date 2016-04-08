package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public class SntpSensor extends SensorComponent<Long, double[]> {

    private ArrayList<InetAddress> servers;
    private boolean forward = false;

    protected SntpSensor(DevicePlugin<Long, double[]> parent) {
        super(parent);
    }

    public void setServers(Collection<String> servers) {
        Set<InetAddress> x = new TreeSet<>(new Comparator<InetAddress>() {
            @Override
            public int compare(InetAddress lhs, InetAddress rhs) {
                if (lhs.equals(rhs))
                    return 0;
                else {
                    int h = rhs.hashCode() - lhs.hashCode();
                    if (h != 0)
                        return h;
                    else
                        return rhs.toString().hashCode() - lhs.toString().hashCode();
                }
            }
        });
        for (String s : servers) {
            try {
                x.add(InetAddress.getByName(s));
            } catch (UnknownHostException e) {
                Log.i(SntpSensor.class.getSimpleName(), "SntpServer unresolved: " + e.getMessage());
            }
        }
        if (x.size() == 0)
            throw new RuntimeException("No internet or NTP addresses unresolvable.");
        this.servers = new ArrayList<>(x);
    }

    public class NtpResp {
        public final int n;
        public final double avg;
        public final double sqavg;

        public NtpResp(int n, double avg, double sqavg) {
            this.n = n;
            this.avg = avg;
            this.sqavg = sqavg;
        }
    }

    public NtpResp compute(String requestId) {
        int t = 1000, n = 0;
        boolean r;
        double avg = 0., sqavg = 0., std;
//        SntpClient[] c = new SntpClient[servers.size()];
        SntpClient c; //= new SntpClient[servers.size()];

        for (int times = 0; times < 4; times++)
            for (int i = 0; i < servers.size() && !Thread.currentThread().isInterrupted(); i++) {
                long ntp = 0;
                c = new SntpClient();
                if (r = c.requestTime(servers.get(i), t)) {
                    ntp = c.getBootTime() - c.getNtpTime() + c.getNtpTimeReference();
                    avg += ntp;
                    sqavg += Math.pow(ntp, 2);
                    n++;
                }
                sensorEvent(getTime().getMonoUTCNanos(), 0, String.format("req:%d, server:%s, timeout:%d, result:%s", requestId.hashCode() % 1024, servers.get(i), t, r ? ntp : "error"));
                try {
                    Thread.sleep((long) (Math.random() * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        avg /= n;
        sqavg /= n;
        std = Math.sqrt(sqavg - Math.pow(avg, 2));

        sensorValue(getTime().getMonoUTCNanos(), new double[] { avg, std, n });

        return new NtpResp(n, avg, sqavg);
    }

    private List<SensorDataEntry<Long, double[]>> superBufferValues = new ArrayList<>(4);
    private List<SensorEventEntry<Long>> superBufferEvents = new ArrayList<>(4);

    @Override
    public void sensorValue(Long time, double[] value) {
        if (forward)
            super.sensorValue(time, value);
        else
            superBufferValues.add(new SensorDataEntry<>(this, time, value));
    }

    @Override
    public void sensorEvent(Long time, int type, String message) {
        Log.v("SntpSensor", "e: " + message);
        if (forward)
            super.sensorEvent(time, type, message);
        else
            superBufferEvents.add(new SensorEventEntry<>(this, time, type, message));
    }

    @Override
    public void switchOnAsync() {
        forward = true;
        for (SensorDataEntry<Long, double[]> i : superBufferValues)
            sensorValue(i.timestamp, i.value);
        for (SensorEventEntry<Long> i : superBufferEvents)
            sensorEvent(i.timestamp, i.code, i.message);
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                0, "Switched on");
    }

    @Override
    public void switchOffAsync() {
        sensorEvent(getTime().getMonoUTCNanos(System.nanoTime()),
                0, "Switched off");
        forward = false;
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Arrays.asList((Object) "avg", "std", "n");
    }
}
