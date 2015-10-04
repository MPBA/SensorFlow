package eu.fbk.mpba.sensorsflows.plugins.outputs;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

/**
 * Byte-coded stream of events/timed-values
 */
public class TCPServerOutput implements OutputPlugin<Long, double[]> {

    public final int ALIGNMENT_BYTE = 0x1E;
    public final int VERSION_BYTE = 0x1;
    public final int DATA_CODE = 100;
    public final int EVENT_CODE = 101;
    public final int HEADER_CODE = 105;

    private final Thread mSTh;
    protected final ServerSocket mSock;
    protected String mTag = null;
    protected volatile Socket mCli = null;
    protected volatile OutputStream mOut = null;

    private int mReceived = 0;
    private int mForwarded = 0;

    public TCPServerOutput(int port) throws IOException {
        this(null, port);
    }

    public TCPServerOutput(InetAddress local, int port) throws IOException {
        mSock = local == null ? new ServerSocket(port, 10) : new ServerSocket(port, 10, local);

        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket a = mSock.accept();
                    mOut = a.getOutputStream();
                    writeDescriptors(mOut);
                    mCli = a;
                } catch (SocketException e) {
                    if (e.getMessage().equals("Socket closed"))
                        Log.v(TCPServerOutput.class.getSimpleName(), "Socket closed");
                    else
                        e.printStackTrace();
                    mCli = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    mCli = null;
                }
            }
        };
        mSTh = new Thread(mRun, TCPServerOutput.class.getSimpleName() + "-ServerThread");
    }

    private List<ISensor> mSensors = null;
    private void writeDescriptors(OutputStream o) throws IOException {
        o.write(new byte[] { ALIGNMENT_BYTE, HEADER_CODE, VERSION_BYTE, (byte) mSensors.size() });
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
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {
        mSTh.start();
        mSensors = streamingSensors;
        mTag = sessionTag.toString();
    }

    @Override
    public void outputPluginFinalize() {
        if (mSTh != null && mSTh.isAlive())
            mSTh.interrupt();
        if (mSock != null && !mSock.isClosed())
            try {
                mSock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {
        mReceived++;
        if (mCli != null) {
            synchronized (mSock) {
                try {
                    mOut.write(new byte[] { ALIGNMENT_BYTE, EVENT_CODE, (byte) mSensors.indexOf(event.sensor) });
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
            synchronized (mSock) {
                try {
                    mOut.write(new byte[] { ALIGNMENT_BYTE, DATA_CODE, (byte) mSensors.indexOf(data.sensor) });
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
