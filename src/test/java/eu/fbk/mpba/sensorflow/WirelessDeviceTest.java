package eu.fbk.mpba.sensorflow;

import org.junit.Assert;
import org.junit.Test;

public class WirelessDeviceTest {
    @Test
    public void test_one() throws InterruptedException {
        Log.enabled = true;

        MockWirelessDevice w = new MockWirelessDevice("WD1", "");

        SensorFlow sf = new SensorFlow();

        MockOutput o = new MockOutput("Out");

        sf.add(o);
        sf.add(w);

        Thread.sleep(1000);

        sf.close();

        Log.l("Sent:     " + w.sentLines);
        Log.l("Received: " + o.receivedLines);
        Assert.assertTrue(w.sentLines.get() == o.receivedLines.get());
    }
}
