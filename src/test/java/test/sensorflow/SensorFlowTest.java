package test.sensorflow;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import eu.fbk.mpba.sensorflow.SensorFlow;

public class SensorFlowTest {
    @Test
    public void theTest() throws InterruptedException {
        SensorFlow sf = new SensorFlow();
        MockOutput mo = new MockOutput("consumer");
        ArrayList<MockInput> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            mi.add(i, new MockInput(null, null));

        sf.add(mo);
        mi.forEach(sf::add);
        sf.routeAll();

        Thread.sleep(1000);

        mi.forEach(MockInput::onStop);

        Thread.sleep(1000);

        long sent = 0;
        for (MockInput i : mi)
            sent += i.sentLines;

        System.out.println(sent);
        System.out.println(mo.receivedLines);
        Assert.assertTrue(sent == mo.receivedLines);
    }
}
