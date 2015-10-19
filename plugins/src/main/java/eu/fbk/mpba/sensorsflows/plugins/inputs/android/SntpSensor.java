package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class SntpSensor extends SensorComponent<Long, double[]> {

    private ArrayList<String> servers;

    protected SntpSensor(DevicePlugin<Long, double[]> parent, Collection<String> servers) {
        super(parent);
        this.servers = new ArrayList<>(servers);
    }

    public void setServers(Collection<String> servers) {
        this.servers = new ArrayList<>(servers);
    }

    public class NtpResp {
        public final int n;
        public final double avg;
        public final double sqsum;

        public NtpResp(int n, double avg, double sqsum) {
            this.n = n;
            this.avg = avg;
            this.sqsum = sqsum;
        }
    }

    public NtpResp compute() {
        int t = 1000;
        boolean r;
        SntpClient[] c = new SntpClient[servers.size()];
        double avg = 0., sqsum = 0.;
        int n = 0;
        for (int i = 0; i < servers.size(); i++) {
            c[i] = new SntpClient();
            if (r = c[i].requestTime(servers.get(i), t)) {
                avg += c[i].getNanoOffset();
                sqsum += Math.pow(c[i].getNanoOffset(), 2);
                n++;
            }
            sensorEvent(getTime().getMonoUTCNanos(), 0, String.format("server:%s, timeout:%d, result:%s", servers.get(i), t, r ? c[i].getNanoOffset() : "error"));
        }

        avg /= n;

        sensorValue(getTime().getMonoUTCNanos(), new double[] { avg, sqsum, n });

        return new NtpResp(n, avg, sqsum);
    }

    @Override
    public void switchOnAsync() {

    }

    @Override
    public void switchOffAsync() {

    }

    @Override
    public List<Object> getValueDescriptor() {
        return Arrays.asList((Object) "avg", "sqsum", "n");
    }
}
