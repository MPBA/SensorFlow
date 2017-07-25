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
//        System.out.println("MockInput onCreate");
        Assert.assertEquals(testStatus, PluginStatus.INSTANTIATED);
        producer = new Thread(() -> {
            try {
                int i = 0;
                Random random = new Random(0);
                while (testStatus != PluginStatus.CLOSED) {
                    while (testStatus == PluginStatus.STARTED) {
                        MockInput.this.pushValue(getTimeSource().getMonoUTCNanos(), new double[]{i++});
                        sentLines++;
                        if (random.nextInt(15) == 0) {
                            MockInput.this.pushLog(getTimeSource().getMonoUTCNanos(), "Random gave 0/15");
                            sentLines++;
                        }
                        Thread.sleep(8);
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // exit
            }
        });
        producer.start();
        testStatus = PluginStatus.CREATED;
    }

    @Override
    public void onStart() {
        Assert.assertTrue(testStatus == PluginStatus.CREATED || testStatus == PluginStatus.STOPPED);
        testStatus = PluginStatus.STARTED;
    }

    @Override
    public void onStop() {
        Assert.assertEquals(testStatus, PluginStatus.STARTED);
        testStatus = PluginStatus.STOPPED;
    }

    @Override
    public void onClose() {
        Assert.assertEquals(testStatus, PluginStatus.STOPPED);
        testStatus = PluginStatus.CLOSED;
    }

    long sentLines = 0;
}
