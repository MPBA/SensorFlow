package test.sensorflow;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;
import eu.fbk.mpba.sensorflow.PluginStatus;

import org.junit.Assert;

import static eu.fbk.mpba.sensorflow.sense.Stream.HEADER_VALUE;


public class MockInput extends Input {

    private PluginStatus status = PluginStatus.INSTANTIATED;
    private Thread producer;

    MockInput(InputGroup parent, String name) {
        super(parent, name, HEADER_VALUE);
    }

    @Override
    public void onCreate() {
        System.out.println("MockInput onCreate");
        Assert.assertEquals(status, PluginStatus.INSTANTIATED);
        producer = new Thread(() -> {
            try {
                int i = 0;
                while (status != PluginStatus.CLOSED) {
                    while (status == PluginStatus.STARTED) {
                        MockInput.this.pushValue(getTimeSource().getMonoUTCNanos(), new double[]{i++});
                        sentLines++;
                        Thread.sleep(16);
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // exit
            }
        });
        producer.start();
        status = PluginStatus.CREATED;
    }

    @Override
    public void onStart() {
        System.out.println("MockInput onStart");
        Assert.assertTrue(status == PluginStatus.CREATED || status == PluginStatus.STOPPED);
        status = PluginStatus.STARTED;
    }

    @Override
    public void onStop() {
        System.out.println("MockInput onStop");
        Assert.assertEquals(status, PluginStatus.STARTED);
        status = PluginStatus.STOPPED;
    }

    @Override
    public void onClose() {
        System.out.println("MockInput onClose");
        Assert.assertEquals(status, PluginStatus.STOPPED);
        status = PluginStatus.CLOSED;
    }

    long sentLines = 0;
}
