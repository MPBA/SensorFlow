package eu.fbk.mpba.sensorflow;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class SensorFlowTest {

    @Test
    public void test_Log() {
        Log.l();
        Log.s();
    }

    @Test
    public void test_sf_oneOutputThreaded() throws InterruptedException {
        Log.enabled = true;
        Log.l(Thread.currentThread().getName());

        SensorFlow sf = new SensorFlow();
        MockOutput mo = new MockOutput("consumer");
        ArrayList<InputGroup> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            mi.add(i, new MockInput(null, "MockInput" + i));

        sf.add(mo);
        sf.add(mi);

        Thread.sleep(1500);

        sf.close();

        long sent = 0;
        for (InputGroup i : mi)
            sent += ((MockInput)i).sentLines;

        Log.l("Sent:     " + sent);
        Log.l("Received: " + mo.receivedLines);
        Assert.assertTrue(sent == mo.receivedLines.get());
    }

    @Test
    public void test_sf_oneOutputNonThreaded() throws InterruptedException {
        Log.enabled = true;
        Log.l(Thread.currentThread().getName());

        SensorFlow sf = new SensorFlow();
        MockOutput mo = new MockOutput("consumer");
        ArrayList<InputGroup> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            mi.add(i, new MockInput(null, "MockInput" + i));

        sf.addInThread(mo);
        sf.add(mi);

        Thread.sleep(1500);

        mi.forEach(InputGroup::onRemoved);

        long sent = 0;
        for (InputGroup i : mi)
            sent += ((MockInput)i).sentLines;

        Log.l("Sent:     " + sent);
        Log.l("Received: " + mo.receivedLines);
        Assert.assertTrue(sent == mo.receivedLines.get());
    }

    @Test
    public void test_sf_routing() throws InterruptedException {
        final int NUM = 10;

        ArrayList<InputGroup> mi = new ArrayList<>();
        for (int i = 0; i < NUM; i++)
            mi.add(i, new MockInput(null, "MockInput" + i));
        ArrayList<Output> mo = new ArrayList<>();
        for (int i = 0; i < NUM; i++)
            mo.add(i, new MockOutput("MockOutput" + i));

        SensorFlow sf = new SensorFlow();
        mo.forEach(sf::addNotRouted);
        sf.addNotRouted(mi);

        mo.forEach((o) -> Assert.assertTrue(((MockOutput)o).receivedLines.get() == 0));

        sf.routeClear();
        mo.forEach((o) -> Assert.assertTrue(((MockOutput)o).receivedLines.get() == 0));

        sf.routeAll();
        Thread.sleep(100);
        mo.forEach((o) -> Assert.assertTrue(((MockOutput)o).receivedLines.get() > 0));

        sf.routeClear();
        Thread.sleep(100);
        final long[] sum = new long[]{0};
        mo.forEach((o) -> sum[0] += ((MockOutput)o).receivedLines.get());

        Assert.assertTrue("One for each assumption failed", sum[0] > NUM);

        Thread.sleep(100);
        mo.forEach((o) -> sum[0] -= ((MockOutput)o).receivedLines.get());

        Assert.assertTrue("Things after routeClear", sum[0] == 0);
    }
}
