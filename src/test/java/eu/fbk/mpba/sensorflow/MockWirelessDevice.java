package eu.fbk.mpba.sensorflow;

import org.junit.Assert;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import eu.fbk.mpba.sensorflow.sense.Stream;
import eu.fbk.mpba.sensorflow.sense.WirelessDevice;

public class MockWirelessDevice extends WirelessDevice {
    private PluginStatus testStatus = PluginStatus.INSTANTIATED;

    MockWirelessDevice(String name, String configuration) {
        super(name, configuration);
    }

    @Override
    public void onCreate() {
        Assert.assertEquals(PluginStatus.INSTANTIATED, testStatus);
        for (int i = 0; i < 10; i++) {
            final Stream s = new Stream(this, Stream.HEADER_VALUE, "MockStream" + i);
            new Thread(() -> {
                try {
                    int i1 = 0;
                    Random random = new Random(0);
                    while (testStatus != PluginStatus.CLOSED) {
                        while (testStatus == PluginStatus.STARTED) {
                            s.pushValue(Input.getTimeSource().getMonoUTCNanos(), new double[]{i1++});
                            sentLines.incrementAndGet();
                            int rnd = random.nextInt(100);
                            if (rnd < 20) {
                                if (rnd < 8) {
                                    s.pushLog(Input.getTimeSource().getMonoUTCNanos(), "Random gave 0-7/15");
                                } else switch (rnd) {
                                    case 9:
                                        s.pushQuality("Buona qualità, yess");
                                        break;
                                    case 10:
                                        s.pushQuality("Cattiva qualità, néh");
                                        break;
                                    default:
                                        s.pushQuality(Input.getTimeSource().getMonoUTCNanos(), ((rnd - 10) * 60 / 10 + 40) + "%");
                                        break;
                                }
                                sentLines.incrementAndGet();
                            }
                            Thread.sleep(8);
                        }
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    // exit
                }
            }).start();
            addStream(s);
        }
        testStatus = PluginStatus.CREATED;
    }

    @Override
    public void onAdded() {
        Assert.assertTrue(testStatus == PluginStatus.CREATED || testStatus == PluginStatus.STOPPED);
        testStatus = PluginStatus.STARTED;
    }

    @Override
    public void onRemoved() {
        Assert.assertEquals(PluginStatus.STARTED, testStatus);
        testStatus = PluginStatus.STOPPED;
    }

    @Override
    public void onClose() {
        Assert.assertEquals(PluginStatus.STOPPED, testStatus);
        testStatus = PluginStatus.CLOSED;
    }

    @Override
    public void connect(Runnable done) {

    }

    AtomicLong sentLines = new AtomicLong(0);
}
