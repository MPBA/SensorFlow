package eu.fbk.mpba.sensorsflows.plugins.inputs.axivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class Wax9Node extends Wax9Receiver implements NodePlugin<Long, double[]> {

    // - LSB first, Little Endian

    // Debug
    private static final String TAG = Wax9Node.class.getSimpleName();
    private String name;
    protected InertialSensors is;
    protected ExtraSensors es;

    public Wax9Node(String name, BluetoothDevice device, BluetoothAdapter adapter, StatusDelegate conn) {
        super(conn, device, adapter);
        this.name = name;
        is = new InertialSensors(this);
        es = new ExtraSensors(this);
    }

    // Operation

    int rcn = 0;
    long basetime = 0;

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    protected void received(final byte[] pack, final int offset, final int bytes) {
        int samplenum;
        int packType, batt;
        long timestamp, press;
        short ax, ay, az, gx, gy, gz, mx, my, mz, temp;

        ByteBuffer bb = ByteBuffer.wrap(pack);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        packType = bb.get();
        samplenum = bb.getShort() & 0xFFFF; // Non signed (& to remove sign extension)
        timestamp = bb.getInt() & 0xFFFFFFFFL;  // Non signed (& to remove sign extension)

        ax = bb.getShort();
        ay = bb.getShort();
        az = bb.getShort();

        gx = bb.getShort();
        gy = bb.getShort();
        gz = bb.getShort();

        mx = bb.getShort();
        my = bb.getShort();
        mz = bb.getShort();

        if (rcn++ == 0)
            basetime = is.getTime().getMonoUTCNanos() - (timestamp * 1_000_000_000L / 65536);

        is.sensorValue(basetime + (timestamp * 1_000_000L / 65536 * 1_000L), new double[]{ samplenum, ax, ay, az, gx, gy, gz, mx, my, mz });

        if (packType == (byte)0x02) {
            batt = bb.getShort(); // Max th. 4200
            temp = bb.getShort(); // Signed milli C°
            press = bb.getInt() & 0xFFFFFFFFL;  // Non signed Pa (& to remove sign extension)

            es.sensorValue(basetime + (timestamp * 1_000_000_000L / 65536), new double[]{ batt * .001, temp * .1, press * .01 });
        }
    }

    public void connect() {
        super.connect(3);
        try {
            Thread.sleep(500);
            command(Commands.startStreaming);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        command(Commands.stopStreaming);
        closeConnection(); // TODO: consider replayability
    }

    @Override
    public void inputPluginInitialize() {
        // connect();
    }

    @Override
    public void inputPluginFinalize() {
        stop();
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return Arrays.asList((SensorComponent<Long, double[]>)is, es);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        closeConnection();
    }

    protected static abstract class ASensor extends SensorComponent<Long, double[]> {

        protected ASensor(NodePlugin<Long, double[]> parent) {
            super(parent);
        }

        @Override
        public void switchOnAsync() {

        }

        @Override
        public void switchOffAsync() {

        }
    }

    protected static class InertialSensors extends ASensor {

        protected InertialSensors(NodePlugin<Long, double[]> parent) {
            super(parent);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object)"snum", "ax", "ay", "az", "gx", "gy", "gz", "mx", "my", "mz");
        }
    }

    protected static class ExtraSensors extends ASensor {

        protected ExtraSensors(NodePlugin<Long, double[]> parent) {
            super(parent);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object)"battery", "temperature", "pressure");
        }
    }
}
