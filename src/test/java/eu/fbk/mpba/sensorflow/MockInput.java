package eu.fbk.mpba.sensorflow;

import org.junit.Assert;

import java.util.Random;

import static eu.fbk.mpba.sensorflow.sense.Stream.HEADER_VALUE;


public class MockInput extends Input {

    private PluginStatus testStatus = PluginStatus.INSTANTIATED;
    private Thread producer;

    MockInput(InputGroup parent, String name) {
        super(parent, name, HEADER_VALUE);
    }

    @Override
    public void onCreate() {
//        System.out.println("MockInput onCreateAndAdded");
        Assert.assertTrue(testStatus == PluginStatus.INSTANTIATED || testStatus == PluginStatus.CLOSED);
        producer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    Random random = new Random(0);
                    while (testStatus != PluginStatus.CLOSED) {
                        while (testStatus == PluginStatus.ADDED) {
                            MockInput.this.pushValue(getTimeSource().getMonoUTCNanos(), new double[]{i++});
                            sentLines++;
                            if (random.nextInt(15) == 0) {
                                MockInput.this.pushLog(getTimeSource().getMonoUTCNanos(), "Random gave 0/15");
                                sentLines++;
                            }
                            Thread.sleep(8);
                        }
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    // exit
                }
            }
        });
        producer.start();
        testStatus = PluginStatus.CREATED;
    }

    @Override
    public void onAdded() {
        Assert.assertTrue(testStatus == PluginStatus.CREATED || testStatus == PluginStatus.REMOVED);
        testStatus = PluginStatus.ADDED;
    }

    @Override
    public void onRemoved() {
        Assert.assertEquals(PluginStatus.ADDED, testStatus);
        testStatus = PluginStatus.REMOVED;
    }

    @Override
    public void onClose() {
        Assert.assertEquals(PluginStatus.REMOVED, testStatus);
        testStatus = PluginStatus.CLOSED;
        try {
            producer.interrupt();
            producer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getThreadId() {
        return producer.getId();
    }

    long sentLines = 0;
}
