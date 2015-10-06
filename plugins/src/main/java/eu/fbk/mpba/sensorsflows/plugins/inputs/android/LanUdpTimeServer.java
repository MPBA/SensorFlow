package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LanUdpTimeServer {
    public static final int timePort = 12865;
    public static final int namePort = 12866;
    public static final int nameAnsPort = 12867;

    private static final byte _reqName = 36;
    private static final byte _reqTime = 38;
    private static final byte _resTime = 39;

    private DatagramSocket _listenerTime;
    private DatagramSocket _listenerName;
    private InetSocketAddress _groupEP;
    private String _serverName;
    private long _bootTime;

    int _pings = 0;

    public LanUdpTimeServer() {
        _serverName = "time-server-from-" + System.currentTimeMillis();
        _bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();
        try {
            _groupEP = new InetSocketAddress(InetAddress.getByAddress(new byte[]{0,0,0,0}), timePort);
            _listenerTime = new DatagramSocket(timePort);
            _listenerName = new DatagramSocket(namePort);
        } catch (IOException e) {
            Log.wtf(LanUdpTimeServer.class.getSimpleName(), "Banana error. find-me:8934rjh938hr4");
        }
    }

    public int getPings() {
        return _pings;
    }

    public String getServerName()

    {
        return _serverName;
    }
    public void setServerName(String value)
         {
        _serverName = value;
    }
    protected long getMonoTime() {
        return System.nanoTime() + _bootTime;
    }

    // 0 1 2 3 4  5 6 7 8
    // R t1t1t1t1 t2t2t2t2

    public InetSocketAddress BeginListening() {
        try {
            Recall(_listenerName);
            Recall(_listenerTime);
            _serverName = _listenerTime.getLocalAddress().toString();
            return (InetSocketAddress)_listenerTime.getLocalSocketAddress();
        } catch (Exception e) {
            return null;
        }
    }

    public InetSocketAddress BeginListening(String serverName) {
        InetSocketAddress r = BeginListening();
        this._serverName = serverName;
        return r;
    }

    protected static long[] receive(DatagramSocket s) throws InterruptedException, IOException {
        DatagramPacket p = new DatagramPacket(new byte[TimePacket.SIZE], TimePacket.SIZE);
        long c;
        try {
            s.receive(p);
            c = System.currentTimeMillis();
            byte[] d = p.getData();
            TimePacket y = new TimePacket(d);
            if (y.first != RES_TIME)
                Log.d("ALE TIME", "OUT of place packet f:" + y.first + " t:" + y.t3);
            else {
                //Log.v("TIMEX", "recv t2:" + y.t2t1 + " t3:" + (y.t3 - __init) + " c:" + (c - __init));
                return new long[]{y.t2t1, y.t3, c};
            }
        } catch (SocketTimeoutException e) {
            Log.v("ALE TIME", "SocketTimeout");
        }
        return null;
    }
    protected static List<Pair<InetAddress, String>> receiveServers() {
        Log.v("ALE TIME", "computeOffsetReceiver");
        DatagramPacket p = new DatagramPacket(new byte[512], 512);
        DatagramSocket s = null;
        List<Pair<InetAddress, String>> ret = new ArrayList<>();
        try {
            s = new DatagramSocket(NAME_ANS_UDP_PORT);
            s.setBroadcast(true);
            s.setSoTimeout(2000);
            for (int i = 0; i < 20; i++) {
                s.receive(p);
                s.setSoTimeout(500);
                ByteBuffer b = ByteBuffer.allocate(p.getData().length);
                b.put(p.getData());
                ret.add(new Pair<>(p.getAddress(), b.asCharBuffer().toString()));
            }
        } catch (SocketTimeoutException e) {
            Log.d("ALE TIME", "R Timeout");
        } catch (SocketException e) {
            Log.d("ALE TIME", "R SocketException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ALE TIME", "R IOException");
            e.printStackTrace();
        } finally {
            if (s != null)
                s.close();
        }
        return ret;
    }

    void Received(long t2, byte[] b, InetAddress s) {

        if (b[0] == _reqTime) {
            OnEventHappened(_reqTime, s.toString());

            long t1 = ByteBuffer.wrap(b).getLong(1);

            ByteBuffer x = ByteBuffer.allocate(9);
            x.put(_resTime);
            x.putLong(t2 - t1);

            DatagramSocket so = null;
            try {
                so = new DatagramSocket();
                so.connect(s, timePort);

                long t3 = getMonoTime();
                x.putLong(t3);
                so.send(new DatagramPacket(x.array(), 9));

            } catch (SocketException e) {
                Log.d("ALE TIME", "S SocketException");
                e.printStackTrace();
            } catch (UnknownHostException e) {
                Log.d("ALE TIME", "S UnknownHostException");
                e.printStackTrace();
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("ALE TIME", "S IOException");
                e.printStackTrace();
            } finally {
                if (so != null)
                    so.close();
            }


            Recall(r.AsyncState);
            _pings++;
        } else if (b[0] == _reqName) {
            OnEventHappened(_reqName, e.Address.ToString() + ":" + e.Port.ToString());
            Recall(r.AsyncState);
            b = Encoding.ASCII.GetBytes(_serverName);
            ((UdpClient) r.AsyncState).Send(b, b.Length, new IPEndPoint(e.Address, nameAnsPort));
        } else {
            OnEventHappened(4, "Problem: received a " + b[0] + " there may be an other network application that works on the same channel.");
            Recall(r.AsyncState);
        }

    }

    private void Recall(Object r) {
        ((UdpClient) r).BeginReceive(new AsyncCallback(Received), ((UdpClient) r));
    }

    void OnEventHappened(int i, String m) {
        if (EventHappened != null)
            EventHappened.Invoke(i, m);
    }

    public interface Action<T,U> {
        void Invoke(T a, U b);
    }

    public Action<Integer, String> EventHappened;
}