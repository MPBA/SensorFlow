package eu.fbk.mpba.sensorflow;

import org.junit.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

public class MockOutput implements Output {
    private final String name;
    private String sessionId;
    private HashSet<Input> testLinkedInputs;

    MockOutput(String name) {
        this.name = name;
        this.testLinkedInputs = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onCreate(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void onClose() {
        testLinkedInputs.forEach((i) -> Log.l(i.getName()));
        Assert.assertTrue(testLinkedInputs.isEmpty());
    }

    final HashMap<Input, Long> lastValueTimes = new HashMap<>();
    final HashMap<Input, Long> lastLogTimes = new HashMap<>();

    @Override
    public void onInputAdded(Input input) {
        Assert.assertFalse(input.getName() + " already present in n elements: " + testLinkedInputs.size(),
                testLinkedInputs.contains(input));
        testLinkedInputs.add(input);
        lastValueTimes.put(input, 0L);
        lastLogTimes.put(input, 0L);
    }

    @Override
    public void onInputRemoved(Input input) {
        Assert.assertTrue(testLinkedInputs.contains(input));
        testLinkedInputs.remove(input);
    }

    @Override
    public void onValue(Input input, long timestamp, double[] value) {
        Assert.assertTrue(lastValueTimes.get(input) < timestamp);
        lastValueTimes.put(input, timestamp);
        receivedLines.incrementAndGet();
    }

    @Override
    public void onLog(Input input, long timestamp, String text) {
        Assert.assertTrue(lastLogTimes.get(input) < timestamp);
        lastValueTimes.put(input, timestamp);
        receivedLines.incrementAndGet();
    }

    AtomicLong receivedLines = new AtomicLong(0);
}
