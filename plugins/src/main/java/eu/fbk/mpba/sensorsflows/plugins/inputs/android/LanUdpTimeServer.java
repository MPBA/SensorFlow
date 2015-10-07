package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class LanUdpTimeServer {
    public static final int timePort = 12865;
    public static final int namePort = 12866;
    public static final int nameAnsPort = 12867;

    private static final byte _reqName = 36;
    private static final byte _reqTime = 38;
    private static final byte _resTime = 39;

    private static final String T = "AleLUTS";

    private String _serverName;
    private long _bootTime;

    private C name;
    private C time;

    int _pings = 0;

    public LanUdpTimeServer() {
        _serverName = "time-server-from-" + System.currentTimeMillis();
        _bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();
        Log.v(T, "LanUdpTimeServer");
    }

    public int getPings() {
        return _pings;
    }

    public String getServerName()

    {
        return _serverName;
    }

    public void setServerName(String value) {
        _serverName = value;
    }

    protected long getMonoTime() {
        return (System.nanoTime() + _bootTime);
    }

    // 0 1 2 3 4  5 6 7 8
    // R t1t1t1t1 t2t2t2t2

    public InetSocketAddress BeginListening() {
        Log.v(T, "BeginListening");
        try {
            name = listen(namePort);
            time = listen(timePort);
            return (InetSocketAddress) time.socket.getLocalSocketAddress();
        } catch (Exception e) {
            Log.v(T, "" + e.getMessage());
            return null;
        }
    }

    private void receive(DatagramSocket r) {
        Log.v(T, "receive " + r.getLocalPort());
        boolean listening = true;
        long t2;
        byte[] b;
        // loop till stop
        while (!Thread.interrupted()) {
            try {
                DatagramPacket p = new DatagramPacket(b = new byte[9], 9);
                r.receive(p);
                t2 = getMonoTime();
                if (b[0] == _reqTime) {
                    OnEventHappened(_reqTime, p.getAddress().toString());
                    long t1 = ByteBuffer.wrap(b).getLong(1);
                    ByteBuffer x = ByteBuffer.wrap(new byte[17]);
                    x.put(_resTime);
                    x.putLong(t2 - t1);

                    //DatagramSocket so;
                    try {
                        DatagramPacket pp = new DatagramPacket(x.array(), 17);

                        InetSocketAddress s = new InetSocketAddress(p.getAddress(), timePort);
                        //so = new DatagramSocket();
                        r.connect(s);

                        long t3 = getMonoTime();
                        x.putLong(t3);

                        Log.v(T, "sending to " + r.getInetAddress() + ":" + r.getLocalPort() + ":" + r.getPort());
                        r.send(pp);
                        Log.v(T, "sent resTime " + (t2 - t1) + " " + t3);

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
                        //if (so != null)
                        //    so.close();
                        _pings++;
                    }
                } else if (b[0] == _reqName) {
                    OnEventHappened(_reqName, p.getAddress().toString());
                    DatagramSocket so = null;
                    try {
                        byte[] bb = _serverName.getBytes(Charset.forName("ASCII"));
                        InetSocketAddress s = new InetSocketAddress(p.getAddress(), nameAnsPort);
                        so = new DatagramSocket();
                        so.connect(s);
                        Log.v(T, "sending name");
                        so.send(new DatagramPacket(bb, bb.length));
                        Log.v(T, "sent name");

                    } catch (SocketException e) {
                        Log.d("ALE TIME", "S SocketException");
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        Log.d("ALE TIME", "S UnknownHostException");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("ALE TIME", "S IOException");
                        e.printStackTrace();
                    } finally {
                        if (so != null)
                            so.close();
                    }
                } else {
                    OnEventHappened(4, "Problem: received a " + b[0] + " there may be an other network application that works on the same channel.");
                }
            } catch (SocketException e) {
                if (!e.getMessage().equals("Socket closed"))
                    e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void StopListening() {
        close();
    }

    private class C {
        public DatagramSocket socket;
        public Thread thread;
    }

    private C listen(final int port) {
        final C or = new C();
        try {
            or.socket = new DatagramSocket(port);
            or.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    receive(or.socket);
                }
            });
            or.thread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return or;
    }

    void OnEventHappened(int i, String m) {
        Log.v(T, "OnEventHappened: " + i + " " + m);
        if (EventHappened != null)
            EventHappened.Invoke(i, m);
    }

    public interface Action<T, U> {
        void Invoke(T a, U b);
    }

    public Action<Integer, String> EventHappened;

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        try {
            name.thread.interrupt();
            name.socket.close();
        } catch (Exception ignored) {

        }
        try {
            time.thread.interrupt();
            time.socket.close();
        } catch (Exception ignored) {

        }
    }
}