package eu.fbk.mpba.sensorflow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class InputUnitTest {

    private final String SESSION_ID = "TestSession";

    @Test
    public void test_muting() throws InterruptedException {
        SensorFlow sf = new SensorFlow(SESSION_ID);

        MockInput i = new MockInput(null, null);
        MockOutput o = new MockOutput("Output");

        sf.add(o);
        sf.add(i);

        assertFalse(i.isMuted());
        long before = o.receivedLines.get();
        Thread.sleep(100);

        i.mute();
        assertTrue(i.isMuted());
        Thread.sleep(50);
        long after = o.receivedLines.get();

        Thread.sleep(75);
        assertTrue(i.isMuted());
        assertEquals(after, o.receivedLines.get());
        assertNotEquals(before, after);
        assertTrue(before < after);

        i.unmute();
        Thread.sleep(75);
        assertTrue(after < o.receivedLines.get());
        assertTrue(before < o.receivedLines.get());
        assertFalse(i.isMuted());

        sf.close();
    }

    @Test
    public void test_timeSource() throws InterruptedException {
        long monoUTCNanos = Input.getTimeSource().getMonoUTCNanos();
        Thread.sleep(0, 10);
        long monoUTCNanos1 = Input.getTimeSource().getMonoUTCNanos();
        assertTrue(monoUTCNanos <= monoUTCNanos1 - 10);

        long bootNanos =
                System.currentTimeMillis() * 1_000_000L
                        - System.nanoTime();

        // 100ms
        assertTrue(Math.abs(bootNanos - Input.getTimeSource().getMonoUTCNanos(0)) < 100_000_000L);

        // 100ms
        assertTrue(Math.abs(
                System.currentTimeMillis() * 1_000_000L
                        - Input.getTimeSource().getMonoUTCNanos(System.nanoTime()))
                < 100_000_000L);
    }

    @Test
    public void test_getHeader_constructors() {
        String[] h = new String[]{"x", "y", "z", "x", "y", "z", "x", "y", "z", "x", "y", "z"};
        Input i1 = new Input(Arrays.asList(h)) {
            @Override
            public void onCreate() {

            }

            @Override
            public void onAdded() {

            }

            @Override
            public void onRemoved() {

            }

            @Override
            public void onClose() {

            }
        };
        Input i2 = new Input(null, Arrays.asList(h)) {
            @Override
            public void onCreate() {

            }

            @Override
            public void onAdded() {

            }

            @Override
            public void onRemoved() {

            }

            @Override
            public void onClose() {

            }
        };
        assertArrayEquals(h, i1.getHeader().toArray(new String[h.length]));
        assertArrayEquals(h, i2.getHeader().toArray(new String[h.length]));
    }
}
