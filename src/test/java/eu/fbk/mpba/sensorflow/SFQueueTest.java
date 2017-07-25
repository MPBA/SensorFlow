package eu.fbk.mpba.sensorflow;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SFQueueTest {
    @Test
    public void testSimple() {

    }

    @Test
    public void testComplex() throws InterruptedException {
        Log.enabled = true;

        Output o = new Output() {
            @Override
            public void onCreate(String sessionId) { }
            @Override
            public String getName() { return null; }
            @Override
            public void onClose() { }

            Input last = null;

            @Override
            public void onInputAdded(Input input) {
                Assert.assertTrue("Input not removed before adding new one.", last == null);
                last = input;
            }

            @Override
            public void onInputRemoved(Input input) {
                Assert.assertTrue("Input not added before removing it.", last != null);
                last = null;
            }

            long lastTime = 0;

            @Override
            public void onValue(Input input, long timestamp, double[] value) {
                Assert.assertTrue("Value without an input.", last != null);
                Assert.assertTrue("Different input sending values.", last == input);
                Assert.assertTrue("Wrong name composed with index.", input.getName().equals("MockInput" + (int) value[0]));
                Assert.assertTrue("Time not monotonic.", timestamp >= lastTime);
                Assert.assertTrue("Time not strictly monotonic.", timestamp > lastTime);
                Assert.assertTrue(value[1] == 1 || value[1] == 2);
            }

            int lastLogI = -1;

            @Override
            public void onLog(Input input, long timestamp, String text) {
                Assert.assertTrue("Log without an input.", last != null);
                Assert.assertTrue("Different input sending logs.", last == input);
                Assert.assertTrue("Time not monotonic.", timestamp >= lastTime);
                Assert.assertTrue("Time not strictly monotonic.", timestamp > lastTime);
                Assert.assertTrue("Wrong text", text.startsWith("HiPedro "));
                int i = Integer.parseInt(text.substring("HiPedro ".length()), 10);
                Assert.assertTrue("Wrong index", i > lastLogI);
                lastLogI = i;
            }
        };

        ArrayList<Input> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockInput in = new MockInput(null, "MockInput" + i);
            mi.add(i, in);
        }

        final SFQueue q = new SFQueue(o, 1000, false);
        new Thread(() -> {
            try {
                //noinspection InfiniteLoopStatement
                while (true)
                    q.pollToHandler(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        for (int i = 0; i < mi.size() * 1000; i++) {
            if (i % 1000 == 0) {
                q.putAdded(mi.get(i / 1000));
            } else if (i % 1000 == 999) {
                q.putRemoved(mi.get(i / 1000));
            } else {
                if (i % 3 == 0) {
                    q.put(mi.get(i / 1000), Input.getTimeSource().getMonoUTCNanos(), "HiPedro " + i);
                } else {
                    q.put(mi.get(i / 1000), Input.getTimeSource().getMonoUTCNanos(), new double[]{i / 1000, i % 3, i});
                }
            }
        }
    }
}
