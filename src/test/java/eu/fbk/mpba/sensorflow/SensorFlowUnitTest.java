package eu.fbk.mpba.sensorflow;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SensorFlowUnitTest {

    private final String SESSION_ID = "TestSession";
    private List<InputGroup> inputs = new ArrayList<>();
    private List<Output> outputs = new ArrayList<>();

    @Before
    public void setup() {
        for (int i = 0; i < 10; i++)
            inputs.add(i, new MockInput(null, "MockInput" + i));

        for (int i = 0; i < 10; i++)
            outputs.add(i, new MockOutput("MockOutput" + i));
    }

    @Test
    public void test_getSessionTag_finalize() throws Throwable {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        assertEquals(SESSION_ID, sf.getSessionTag());

        //noinspection FinalizeCalledExplicitly
        sf.finalize();
    }

    @Test
    public void test_add_output() {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        sf.add(outputs.get(3));
        assertEquals(1, sf.getOutputs().size());
        sf.addNotRouted(outputs.get(5));
        assertEquals(2, sf.getOutputs().size());
        sf.add(outputs.get(3));
        assertEquals(2, sf.getOutputs().size());
        sf.addNotRouted(outputs.get(7));
        assertEquals(3, sf.getOutputs().size());

        for (Output p : outputs) {
            sf.add(p);
        }

        assertEquals(outputs.size(), sf.getOutputs().size());

        sf.close();
    }

    @Test
    public void test_add_input() {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        sf.add(inputs.get(3));
        assertEquals(1, sf.getInputs().size());
        sf.addNotRouted(inputs.get(5));
        assertEquals(2, sf.getInputs().size());
        sf.add(inputs.get(3));
        assertEquals(2, sf.getInputs().size());
        sf.addNotRouted(inputs.get(7));
        assertEquals(3, sf.getInputs().size());

        sf.add(inputs);
        assertEquals(inputs.size(), sf.getInputs().size());

        sf.close();
    }

    @Test
    public void test_remove_output() {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        for (Output p : outputs) {
            sf.addNotRouted(p);
        }


        sf.remove(outputs.get(3));
        assertEquals(outputs.size() - 1, sf.getOutputs().size());
        sf.remove(outputs.get(5));
        assertEquals(outputs.size() - 2, sf.getOutputs().size());
        sf.remove(outputs.get(3));
        assertEquals(outputs.size() - 2, sf.getOutputs().size());
        sf.remove(outputs.get(7));
        assertEquals(outputs.size() - 3, sf.getOutputs().size());

        sf.close();
    }

    @Test
    public void test_remove_input() {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        for (InputGroup p : inputs) {
            sf.addNotRouted(p);
        }


        sf.remove(inputs.get(3));
        assertEquals(inputs.size() - 1, sf.getInputs().size());
        sf.remove(inputs.get(5));
        assertEquals(inputs.size() - 2, sf.getInputs().size());
        sf.remove(inputs.get(3));
        assertEquals(inputs.size() - 2, sf.getInputs().size());

        sf.close();
    }

    @Test
    public void test_routes() {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        for (InputGroup p : inputs) {
            sf.addNotRouted(p);
        }

        for (Output p : outputs) {
            sf.addNotRouted(p);
        }


        // Add all routes
        sf.routeAll();

        // Routes everywhere
        for (InputGroup i : inputs)
            for (Output o : outputs)
                assertTrue(sf.isRouted((Input) i, o));

        // Remove routes from x
        for (int i = 0; i < inputs.size(); i++)
            for (int j = 0; j < outputs.size(); j++)
                if (i + j == inputs.size())
                    sf.removeRoute((Input) inputs.get(i), outputs.get(j));

        // No Routes in x places, Routes in x^ places
        for (int i = 0; i < inputs.size(); i++)
            for (int j = 0; j < outputs.size(); j++)
                assertEquals("i:" + i + " j:" + j,
                        i + j != inputs.size(),
                        sf.isRouted((Input) inputs.get(i), outputs.get(j)));

        for (int i = 0; i < inputs.size(); i++)
            for (int j = 0; j < outputs.size(); j++)
                if (i + j == inputs.size())
                    sf.addRoute((Input) inputs.get(i), outputs.get(j));

        // Routes everywhere
        for (InputGroup i : inputs)
            for (Output o : outputs)
                assertTrue(sf.isRouted((Input) i, o));

        // Remove all routes
        sf.routeClear();

        // No Routes anywhere
        for (InputGroup i : inputs)
            for (Output o : outputs)
                assertFalse(sf.isRouted((Input) i, o));

        assertFalse(sf.isRouted(null, outputs.get(0)));
        assertFalse(sf.isRouted((Input)inputs.get(0), null));
        assertFalse(sf.isRouted(null, null));

        sf.close();
    }

    @Test
    public void test_isOutputEnabled() {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        for (Output p : outputs) {
            sf.addNotRouted(p);
        }

        for (Output o : outputs) {
            assertTrue(o.getName(), sf.isOutputEnabled(o));
        }


        sf.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_disableOutput_enableOutput() throws InterruptedException {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        for (Output p : outputs) {
            sf.addInThreadNotRouted(p);
        }

        for (InputGroup p : inputs) {
            sf.addNotRouted(p);
        }

        sf.routeAll();

        long dataReceivedToggle = 0;
        long dataReceivedActive = 0;

        for (int i = 0; i < outputs.size(); i++)
            if (i % 2 == 0) {
                sf.disableOutput(outputs.get(i));
                dataReceivedToggle += ((MockOutput) outputs.get(i)).receivedLines.get();
            } else
                dataReceivedActive += ((MockOutput) outputs.get(i)).receivedLines.get();

        Thread.sleep(100);

        for (int i = 0; i < outputs.size(); i++)
            if (i % 2 == 0) {
                dataReceivedToggle -= ((MockOutput) outputs.get(i)).receivedLines.get();
                assertFalse(sf.isOutputEnabled(outputs.get(i)));
            } else {
                dataReceivedActive -= ((MockOutput) outputs.get(i)).receivedLines.get();
                assertTrue(sf.isOutputEnabled(outputs.get(i)));
            }

        dataReceivedActive *= -1;
        dataReceivedToggle *= -1;

        // No data received after disable
        assertEquals(0, dataReceivedToggle);

        // Data received meanwhile, at least one per InputGroup
        assertTrue("dataReceivedActive: " + dataReceivedActive, dataReceivedActive > inputs.size());

        dataReceivedToggle = 0;
        dataReceivedActive = 0;

        for (int i = 0; i < outputs.size(); i++)
            if (i % 2 == 0) {
                dataReceivedToggle += ((MockOutput) outputs.get(i)).receivedLines.get();
                sf.enableOutput(outputs.get(i));
            } else
                dataReceivedActive += ((MockOutput) outputs.get(i)).receivedLines.get();

        Thread.sleep(100);

        for (int i = 0; i < outputs.size(); i++) {
            if (i % 2 == 0) {
                dataReceivedToggle -= ((MockOutput) outputs.get(i)).receivedLines.get();
            } else
                dataReceivedActive -= ((MockOutput) outputs.get(i)).receivedLines.get();
            assertTrue(sf.isOutputEnabled(outputs.get(i)));
        }

        dataReceivedActive *= -1;
        dataReceivedToggle *= -1;

        // Data received after enable, at least one per InputGroup
        assertTrue("dataReceivedToggle: " + dataReceivedToggle, dataReceivedToggle > inputs.size());

        // Data received meanwhile, at least one per InputGroup
        assertTrue(dataReceivedActive > inputs.size());

        try {
            sf.remove(outputs.get(0));
            sf.disableOutput(outputs.get(0));
        } finally {
            sf.close();
        }
    }

    private void assertThread(long id) {
        assertEquals(Thread.currentThread().getId(), id);
    }

    private void assertThread(Input i) {
        assertEquals(Thread.currentThread().getId(), ((MockInput)i).getThreadId());
    }

    @Test
    public void test_InThread() throws InterruptedException {
        SensorFlow sf = new SensorFlow(SESSION_ID);
        for (InputGroup p : inputs) {
            sf.add(p);
        }


        final long id = Thread.currentThread().getId();

        final boolean[] cond = new boolean[4];

        sf.addInThread(new MockOutput("CustomMockOutput") {
            @Override
            public void onValue(Input input, long timestamp, double[] value) {
                assertThread(input);
                cond[0] = true;
            }

            @Override
            public void onLog(Input input, long timestamp, String text) {
                assertThread(input);
                cond[1] = true;
            }

            @Override
            public void onInputAdded(Input input) {
                assertThread(id);
                cond[2] = true;
            }

            @Override
            public void onInputRemoved(Input input) {
                assertThread(id);
                cond[3] = true;
            }
        });

        Thread.sleep(300);

        sf.close();

        assertTrue(cond[2]);
        assertTrue(cond[3]);
        assertTrue(cond[0]);
        assertTrue(cond[1]);
    }

    @Test
    public void test_InThreadNotRouted() {
        SensorFlow sf = new SensorFlow(SESSION_ID);
        for (InputGroup p : inputs) {
            sf.add(p);
        }


        final long id = Thread.currentThread().getId();

        sf.addInThreadNotRouted(new MockOutput("CustomMockOutput") {
            @Override
            public void onValue(Input input, long timestamp, double[] value) {
                assertThread(input);
            }

            @Override
            public void onLog(Input input, long timestamp, String text) {
                assertThread(input);
            }

            @Override
            public void onInputAdded(Input input) {
                assertThread(id);
            }

            @Override
            public void onInputRemoved(Input input) {
                assertThread(id);
            }
        });

        sf.routeAll();

        sf.close();
    }

    @Test
    public void test_NonInThread() {
        SensorFlow sf = new SensorFlow(SESSION_ID);
        for (InputGroup p : inputs) {
            sf.add(p);
        }

        final AtomicLong id = new AtomicLong(-1);

        sf.add(new MockOutput("CustomMockOutput") {

            @Override
            public void onValue(Input input, long timestamp, double[] value) {
                id.compareAndSet(-1, Thread.currentThread().getId());
                assertThread(id.get());
            }

            @Override
            public void onLog(Input input, long timestamp, String text) {
                id.compareAndSet(-1, Thread.currentThread().getId());
                assertThread(id.get());
            }

            @Override
            public void onInputAdded(Input input) {
                id.compareAndSet(-1, Thread.currentThread().getId());
                assertThread(id.get());
            }

            @Override
            public void onInputRemoved(Input input) {
                id.compareAndSet(-1, Thread.currentThread().getId());
                assertThread(id.get());
            }
        });

        sf.close();
    }
}
