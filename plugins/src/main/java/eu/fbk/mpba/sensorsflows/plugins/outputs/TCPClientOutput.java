package eu.fbk.mpba.sensorsflows.plugins.outputs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

/**
 * Byte-coded stream of events/timed-values
 */
public class TCPClientOutput implements OutputPlugin<Long, double[]> {

    private final String mSockSync = "mSockSynchoipew";
    private String mUser;
    private String mPass;
    protected Socket mSock;
    protected String mTag = null;
    protected volatile Socket mCli = null;
    protected volatile OutputStream mOut = null;

    private InetAddress mAddr;
    private int mPort;

    private int mReceived = 0;
    private int mForwarded = 0;

    public TCPClientOutput(InetAddress local, int port, String username, String passphrase) throws IOException {
        mAddr = local;
        mPort = port;
        if (username.length() > 255 || passphrase.length() > 255)
            throw new IllegalArgumentException("Username and password must not be longer than 255 bytes.");
        mUser = username;
        mPass = passphrase;
    }

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        mSensors = streamingSensors;
        mTag = sessionTag.toString();
        try {
            mSock = new Socket(mAddr, mPort);
            OutputStream o = mSock.getOutputStream();
            o.write(mUser.length());
            o.write(mUser.getBytes());
            o.write(mPass.length());
            o.write(mPass.getBytes());
            writeDescriptors(o);
            mCli = mSock;
            mOut = o;
        } catch (IOException e) {
            mSock = null;
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void outputPluginFinalize() {
        if (mSock != null && !mSock.isClosed())
            try {
                mSock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private List<ISensor> mSensors = null;
    private void writeDescriptors(OutputStream o) throws IOException {
        o.write(new byte[] { 0x1E, 105, 0x01, (byte) mSensors.size() });
        o.write(mTag.length());
        o.write(mTag.getBytes());
        for (byte i = 0; i < mSensors.size(); i++) {
            // sensor_info_block: p,h,id
            o.write(new byte[] { 0x1E, 115, i });
            // type
            o.write(mSensors.get(i).getClass().getSimpleName().length());
            o.write(mSensors.get(i).getClass().getSimpleName().getBytes());
            // name
            o.write(mSensors.get(i).toString().length());
            o.write(mSensors.get(i).toString().getBytes());

            List<Object> oo = mSensors.get(i).getValueDescriptor();
            // N
            o.write(oo.size());
            // descriptors
            for (Object d : oo) {
                o.write(d.toString().length());
                o.write(d.toString().getBytes());
            }
        }
    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        mReceived++;
        if (mCli != null) {
            synchronized (mSockSync) {
                try {
                    mOut.write(new byte[] { 0x1E, 101, (byte) mSensors.indexOf(event.sensor) });
                    ByteBuffer b = ByteBuffer.allocate(12);
                    b.putLong(event.timestamp); // Big-Endian java and network
                    b.putInt(event.code); // Big-Endian java and network
                    mOut.write(b.array());
                    mOut.write(event.message.length());
                    mOut.write(event.message.getBytes());
                    mForwarded++;
                } catch (IOException e) {
                    e.printStackTrace();
                    mCli = null;
                }
            }
        }
    }

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {
        mReceived++;
        if (mCli != null) {
            synchronized (mSockSync) {
                try {
                    mOut.write(new byte[]{0x1E, 100, (byte) mSensors.indexOf(data.sensor)});
                    ByteBuffer b = ByteBuffer.allocate(8 + data.value.length * 8);
                    b.putLong(data.timestamp);
                    for (double v : data.value)
                        b.putDouble(v);
                    mOut.write(b.array());
                    mForwarded++;
                } catch (IOException e) {
                    e.printStackTrace();
                    mCli = null;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getReceivedMessagesCount() {
        return mForwarded;
    }

    @Override
    public int getForwardedMessagesCount() {
        return mReceived;
    }
}
