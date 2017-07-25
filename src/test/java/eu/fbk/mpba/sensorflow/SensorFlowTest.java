package eu.fbk.mpba.sensorflow;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class SensorFlowTest {
    @Test
    public void test_sf_oneOutputThreaded() throws InterruptedException {
        Log.enabled = true;
        Log.l(Thread.currentThread().getName());

        SensorFlow sf = new SensorFlow();
        MockOutput mo = new MockOutput("consumer");
        ArrayList<Input> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            mi.add(i, new MockInput(null, "MockInput" + i));

        sf.add(mo);
        sf.add(mi);

        Thread.sleep(1500);

        mi.forEach(Input::onStop);

        long sent = 0;
        for (Input i : mi)
            sent += ((MockInput)i).sentLines;

        Log.l("Sent:     " + sent);
        Log.l("Received: " + mo.receivedLines);
        Assert.assertTrue(sent == mo.receivedLines);
    }

    @Test
    public void test_sf_oneOutputNonThreaded() throws InterruptedException {
        Log.enabled = true;
        Log.l(Thread.currentThread().getName());

        SensorFlow sf = new SensorFlow();
        MockOutput mo = new MockOutput("consumer");
        ArrayList<Input> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            mi.add(i, new MockInput(null, "MockInput" + i));

        sf.addNotThreaded(mo);
        sf.add(mi);

        Thread.sleep(1500);

        mi.forEach(Input::onStop);

        long sent = 0;
        for (Input i : mi)
            sent += ((MockInput)i).sentLines;

        Log.l("Sent:     " + sent);
        Log.l("Received: " + mo.receivedLines);
        Assert.assertTrue(sent == mo.receivedLines);
    }
}
