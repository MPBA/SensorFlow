package eu.fbk.mpba.sensorflow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OutputBufferTest {

    @Test(expected = NullPointerException.class)
    public void test_nullDrain() {
        new OutputBuffer(null, 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_sizeZero() {
        new OutputBuffer(new MockOutput(""), 0, false);
    }

    @Test
    public void test_getName_events() {
        final boolean[] cond = new boolean[2];
        final String name = "CustomMockOutput20957";

        MockOutput o = new MockOutput(name) {
            @Override
            public void onCreate(String sessionId) {
                super.onCreate(sessionId);
                cond[0] = true;
            }

            @Override
            public void onClose() {
                super.onClose();
                cond[1] = true;
            }
        };
        OutputBuffer outputBuffer = new OutputBuffer(o, 10, false);

        outputBuffer.onCreate("TestSession523");
        outputBuffer.onClose();

        assertEquals(name, outputBuffer.getName());

        assertTrue(cond[0]);
        assertTrue(cond[1]);
    }

    @Test
    public void test_sequential() throws InterruptedException {
        final String sequence = "aaavlvalvlvllvvlvlvrvllrvvrrvvvvvvarvllvlvvvvrrvr";
        final boolean[] done = new boolean[]{false};

        final OutputBuffer q = new OutputBuffer(new Output() {
            @Override
            public void onCreate(String sessionId) { }
            @Override
            public String getName() { return null; }

            @Override
            public void onClose() {
                assertTrue(counter == sequence.length());
                done[0] = true;
            }

            int counter = 0;

            @Override
            public void onInputAdded(Input input) {
                assertTrue(sequence.charAt(counter++) == 'a');
            }

            @Override
            public void onInputRemoved(Input input) {
                assertTrue(sequence.charAt(counter++) == 'r');
            }

            @Override
            public void onValue(Input input, long timestamp, double[] value) {
                assertTrue(sequence.charAt(counter++) == 'v');
            }

            @Override
            public void onLog(Input input, long timestamp, String text) {
                assertTrue(sequence.charAt(counter++) == 'l');
            }
        }, sequence.length() + 1, false);

        Input a = new MockInput(null, null);

        for (int i = 0; i < sequence.length(); i++) {
            assertTrue(q.size() == i);
            assertTrue(q.remainingCapacity() == sequence.length() + 1 - i);
            switch (sequence.charAt(i)) {
                case 'a':
                    q.onInputAdded(a);
                    break;
                case 'r':
                    q.onInputRemoved(a);
                    break;
                case 'v':
                    q.onValue(a, Input.getTimeSource().getMonoUTCNanos(), new double[]{1});
                    break;
                case 'l':
                    q.onLog(a, Input.getTimeSource().getMonoUTCNanos(), "hi");
                    break;
            }
            assertTrue(q.size() == i + 1);
            assertTrue(q.remainingCapacity() == sequence.length() - i);
        }

        assertTrue(q.remainingCapacity() == 1);

        for (int i = 0; i < sequence.length(); i++) {
            assertTrue(q.size() == sequence.length() - i);
            assertTrue(q.remainingCapacity() == i + 1);
            q.pollToHandler(1, TimeUnit.SECONDS);
        }

        assertTrue(q.size() == 0);

        q.getHandler().onClose();
        assertTrue(done[0]);
    }

    @Test
    public void test_full_complex() throws InterruptedException {
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
                assertTrue("Input not removed before adding new one.", last == null);
                last = input;
            }

            @Override
            public void onInputRemoved(Input input) {
                assertTrue("Input not added before removing it.", last != null);
                last = null;
            }

            long lastTime = 0;

            @Override
            public void onValue(Input input, long timestamp, double[] value) {
                assertTrue("Value without an input.", last != null);
                assertTrue("Different input sending values.", last == input);
                assertTrue("Wrong name composed with index.", input.getName().equals("MockInput" + (int) value[0]));
                assertTrue("Time not monotonic.", timestamp >= lastTime);
                assertTrue("Time not strictly monotonic.", timestamp > lastTime);
                assertTrue(value[1] == 1 || value[1] == 2);
            }

            int lastLogI = -1;

            @Override
            public void onLog(Input input, long timestamp, String text) {
                assertTrue("Log without an input.", last != null);
                assertTrue("Different input sending logs.", last == input);
                assertTrue("Time not monotonic.", timestamp >= lastTime);
                assertTrue("Time not strictly monotonic.", timestamp > lastTime);
                assertTrue("Wrong text", text.startsWith("HiPedro "));
                int i = Integer.parseInt(text.substring("HiPedro ".length()), 10);
                assertTrue("Wrong index", i > lastLogI);
                lastLogI = i;
            }
        };

        ArrayList<Input> mi = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MockInput in = new MockInput(null, "MockInput" + i);
            mi.add(i, in);
        }

        final OutputBuffer q = new OutputBuffer(o, 1000, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
                    while (true)
                        q.pollToHandler(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        for (int i = 0; i < mi.size() * 1000; i++) {
            if (i % 1000 == 0) {
                q.onInputAdded(mi.get(i / 1000));
            } else if (i % 1000 == 999) {
                q.onInputRemoved(mi.get(i / 1000));
            } else {
                if (i % 3 == 0) {
                    q.onLog(mi.get(i / 1000), Input.getTimeSource().getMonoUTCNanos(), "HiPedro " + i);
                } else {
                    q.onValue(mi.get(i / 1000), Input.getTimeSource().getMonoUTCNanos(), new double[]{i / 1000, i % 3, i});
                }
            }
        }

        q.clear();
    }
}
