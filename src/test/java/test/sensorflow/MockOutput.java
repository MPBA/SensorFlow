package test.sensorflow;

import org.junit.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.Output;

public class MockOutput implements Output {
    private final String name;
    private String sessionId;
    private HashSet<Input> linkedInputs;

    MockOutput(String name) {
        this.name = name;
        this.linkedInputs = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onCreate(String sessionId) {
        System.out.println("MockOutput onCreate sid:" + sessionId);
        this.sessionId = sessionId;
    }

    @Override
    public void onClose() {
        System.out.println("MockOutput onClose");
        Assert.assertTrue(linkedInputs.isEmpty());
    }

    HashMap<Input, Long> lastValueTimes = new HashMap<>();
    HashMap<Input, Long> lastLogTimes = new HashMap<>();

    @Override
    public void onInputAdded(Input input) {
        Assert.assertFalse(linkedInputs.contains(input));
        linkedInputs.add(input);
        lastValueTimes.put(input, 0L);
        lastLogTimes.put(input, 0L);
    }

    @Override
    public void onInputRemoved(Input input) {
        Assert.assertTrue(linkedInputs.contains(input));
        linkedInputs.remove(input);
    }

    @Override
    public void onValue(Input input, long timestamp, double[] value) {
        Assert.assertTrue(lastValueTimes.get(input) < timestamp);
        receivedLines++;
    }

    @Override
    public void onLog(Input input, long timestamp, String text) {
        Assert.assertTrue(lastLogTimes.get(input) < timestamp);
    }

    long receivedLines = 0;
}
