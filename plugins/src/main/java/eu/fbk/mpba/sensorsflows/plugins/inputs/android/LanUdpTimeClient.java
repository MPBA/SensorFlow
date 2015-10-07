package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LanUdpTimeClient {

    final static short DEF_UDP_PORT = 12865;
    final static short NAME_UDP_PORT = 12866;
    final static short NAME_ANS_UDP_PORT = 12867;
    final static byte REQ_NAME = 36;
    final static byte REQ_TIME = 38;
    final static byte RES_TIME = 39;

    final static long bootTime = System.currentTimeMillis() * 1_000_000L - System.nanoTime();

    protected static long getMonoTime() {
        return (System.nanoTime() + bootTime);
    }

    public interface TimeOffsetCallback {
        void end(boolean error, InetAddress server, String serverName, OffsetInfo offset);
    }

    public interface ServersCallback {
        void end(List<Pair<InetAddress, String>> servers);
    }

    public static void searchForServersAsync(final ServersCallback end) {
        final Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // receiver
                        List<Pair<InetAddress, String>> o = receiveServers();
                        end.end(o);
                    }
                });
        t.start();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // sender
                        askForServers();
                    }
                }).start();
    }

    public static void computeOffsetAsync(final TimeOffsetCallback end, final InetAddress host, final String serverName, final int passes) {
        Log.v("ALE TIME", "computeOffsetAsync");
        final Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // receiver
                        OffsetInfo o = null;
                        try {
                            o = computeOffsetReceiver(host == null ? InetAddress.getByName(serverName) : host, passes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        end.end(o == null, host, serverName, o);
                    }
                });
        t.start();
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // sender
                        try {
                            computeOffsetSender(host == null ? InetAddress.getByName(serverName) : host, passes, t);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
    }

    protected static List<Pair<InetAddress, String>> receiveServers() {
        Log.v("ALE TIME", "receiveServers");
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
            Log.d("ALE TIME", "R Timeout (legal servers discovery end)");
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

    protected static void askForServers() {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket(NAME_UDP_PORT);
            s.connect(InetAddress.getByName("255.255.255.255"), NAME_UDP_PORT);
            s.setBroadcast(true);
            s.send(new DatagramPacket(new TimePacket(REQ_NAME, 0, 0).bytes(), TimePacket.SIZE));
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
            if (s != null)
                s.close();
        }
    }

    protected static void computeOffsetSender(InetAddress host, int passes, Thread receiver) {
        Log.v("ALE TIME", "computeOffsetSender");
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
            s.connect(host, DEF_UDP_PORT);

            for (int i = 0; i < passes; i++) {
                send(s, 0);
                Thread.sleep(10);
            }

        } catch (SocketException e) {
            Log.d("ALE TIME", "S SocketException");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d("ALE TIME", "S UnknownHostException");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.d("ALE TIME", "S InterruptedException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ALE TIME", "S IOException");
            e.printStackTrace();
        } finally {
            if (s != null)
                s.close();
            receiver.interrupt();
        }
    }

    protected static OffsetInfo computeOffsetReceiver(InetAddress host, int passes) {
        Log.v("ALE TIME", "computeOffsetReceiver " + host);
        double[] res = new double[passes];
        DatagramSocket s = null;
        int times = 0;
        double mean = 0;
        try {
            s = new DatagramSocket(DEF_UDP_PORT);
            s.connect(host, DEF_UDP_PORT);
            s.setSoTimeout(1000);

            while (times < passes && !Thread.currentThread().isInterrupted()) {
                long[] v = receive(s);
                if (v != null) {
                    mean += res[times] = (v[2] - v[0] - v[1]) / 2.0 / 1000.0;
                    times++;
                    Log.v("ALE TIMEX", "time " + times + ": " + res[times-1] + " tmp_mean " + times + ": " + mean/1000000/times);
                }
            }

        } catch (SocketException e) {
            Log.d("ALE TIME", "R SocketException");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d("ALE TIME", "R UnknownHostException");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.d("ALE TIME", "R InterruptedException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ALE TIME", "R IOException");
            e.printStackTrace();
        } finally {
            if (s != null)
                s.close();
        }

        double stdev = 0;
        if (times > 1) {
            mean /= times;
            for (int i = 0; i < times; i++) {
                stdev += Math.pow(res[i] - mean, 2);
            }
            stdev /= times;
            stdev = Math.pow(stdev, 0.5);
        }
        else
            stdev = Double.POSITIVE_INFINITY;
        return new OffsetInfo(mean / 1_000_000, stdev / 1_000_000, times);
    }

    protected static long send(DatagramSocket s, long sec) throws InterruptedException, IOException {
        //Log.d("ALE TIME", "send");
        long c = getMonoTime();
        s.send(new DatagramPacket(new TimePacket(REQ_TIME, c, sec).bytes(), TimePacket.SIZE));
        //Log.v("TIMEX", "send " + (c - __init));
        return c;
    }

    protected static long[] receive(DatagramSocket s) throws InterruptedException, IOException {
        DatagramPacket p = new DatagramPacket(new byte[TimePacket.SIZE], TimePacket.SIZE);
        long c;
        try {
            Log.v("ALE TIME", "receivin V");
            s.receive(p);
            c = getMonoTime();
            Log.v("ALE TIME", "received ^");
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

    static class TimePacket {
        public TimePacket(byte first, long t2t1, long t3) {
            this.first = first;
            this.t2t1 = t2t1;
            this.t3 = t3;
        }

        public TimePacket(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.allocate(SIZE);
            buffer.put(bytes);
            buffer.flip();
            first = buffer.get();
            t2t1 = buffer.getLong();
            t3 = buffer.getLong();
        }

        public static final int SIZE = Byte.SIZE + Long.SIZE + Long.SIZE;

        public byte first;
        public long t2t1;
        public long t3;

        public byte[] bytes() {
            ByteBuffer buffer = ByteBuffer.allocate(SIZE);
            buffer.put(first);
            buffer.putLong(t2t1);
            buffer.putLong(t3);
            return buffer.array();
        }
    }

    public static class OffsetInfo {
        public double average;
        public double stDev;
        public int passes;

        public OffsetInfo(double avg, double std, int passes) {
            average = avg;
            stDev = std;
            this.passes = passes;
        }
    }
}