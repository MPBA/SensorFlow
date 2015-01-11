package eu.fbk.mpba.sensorsflows;

import junit.framework.Assert;
import junit.framework.TestCase;

import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.stubs.DeviceStub;
import eu.fbk.mpba.sensorsflows.stubs.OutputStub;

public class FlowsManTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();

        Assert.assertEquals(" Once created the engine goes STANDBY",
                instance.getStatus(), EngineStatus.STANDBY);
    }

    public void tearDown() throws Exception {

    }

    public void testAddDevice() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();

        DeviceStub d = new DeviceStub("Saxophone", 10);
        Assert.assertEquals("There are no devices", instance._userDevices.size(), 0);
        instance.addDevice(d);
        Assert.assertEquals("There is a device", instance._userDevices.size(), 1);
        DeviceStub e = new DeviceStub("Microphone", 3);
        instance.addDevice(e);
        Assert.assertEquals("There are 2 devices", instance._userDevices.size(), 2);

        instance.start();

        DeviceStub f = new DeviceStub("Uphold", 3);
        boolean error = false;
        try {
            instance.addDevice(f);

        } catch (UnsupportedOperationException ex) {
            ex.printStackTrace();
            error = true;
        }
        Assert.assertTrue("Causes an exception", error);
        Assert.assertEquals("There are 2 devices", instance._userDevices.size(), 2);
    }

    public void testAddLink() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();

        DeviceStub d = new DeviceStub("Saxophone", 10);
        DeviceStub e = new DeviceStub("Microphone", 3);
        DeviceStub f = new DeviceStub("Uphold", 3);
        instance.addDevice(d);
        instance.addDevice(e);
        instance.addDevice(f);
        OutputStub o = new OutputStub("Saxophone");
        OutputStub p = new OutputStub("Microphone");
        OutputStub q = new OutputStub("Uphold");
        instance.addOutput(o);
        instance.addOutput(p);
        instance.addOutput(q);

        // TODO HEAD Add link test after adding sensors to the stub

    }

    public void testAddOutput() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();

        OutputStub d = new OutputStub("Saxophone");
        Assert.assertEquals("There are no outputs", 0, instance._userOutputs.size());
        instance.addOutput(d);
        Assert.assertEquals("There is an output", 1, instance._userOutputs.size());
        OutputStub e = new OutputStub("Microphone");
        instance.addOutput(e);
        Assert.assertEquals("There are 2 outputs", 2, instance._userOutputs.size());

        instance.start();

        OutputStub f = new OutputStub("Uphold");
        boolean error = false;
        try {
            instance.addOutput(f);

        } catch (UnsupportedOperationException ex) {
            ex.printStackTrace();
            error = true;
        }
        Assert.assertTrue("Causes an exception", error);
        Assert.assertEquals("There are 2 outputs", instance._userOutputs.size(), 2);
    }

    public void testGetDevices() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();

        DeviceStub d = new DeviceStub("Saxophone", 10);
        DeviceStub e = new DeviceStub("Microphone", 3);
        DeviceStub f = new DeviceStub("Uphold", 3);
        instance.addDevice(d);
        instance.addDevice(e);
        instance.addDevice(f);

        int numba = 0;
        for (DeviceImpl<Integer, Double> ignored : instance.getDevices()) {
            numba++;
        }

        Assert.assertEquals("There are three devices", numba, 3);

        instance.start();

        numba = 0;
        for (DeviceImpl<Integer, Double> ignored : instance.getDevices()) {
            numba++;
        }

        Assert.assertEquals("There are three devices", numba, 3);
    }

    public void testGetOutputs() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();

        OutputStub d = new OutputStub("Saxophone");
        OutputStub e = new OutputStub("Microphone");
        OutputStub f = new OutputStub("Uphold");
        instance.addOutput(d);
        instance.addOutput(e);
        instance.addOutput(f);

        int numba = 0;
        for (OutputImpl ignored : instance.getOutputs()) {
            numba++;
        }

        Assert.assertEquals("There are three outputs", 3, numba);

        instance.start();

        numba = 0;
        for (OutputImpl ignored : instance.getOutputs()) {
            numba++;
        }

        Assert.assertEquals("There are three outputs", 3, numba);
    }

    public void testIsPaused() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();
        Assert.assertFalse("Not paused before start", instance.isPaused());
        instance.start();
        Assert.assertFalse("Not paused after start", instance.isPaused());
        EngineStatus pre = instance.getStatus();
        instance.setPaused(true);
        Assert.assertTrue("Paused after setPaused call, status was " + pre.toString(), instance.isPaused());
    }

    public void testSetPaused() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();
        instance.setPaused(true);
        Assert.assertTrue("Paused works ever", instance.isPaused());
        instance.start();
        Assert.assertTrue("Paused works ever", instance.isPaused());
        instance.setPaused(true);
        Assert.assertTrue("Paused works ever", instance.isPaused());
        instance.setPaused(false);
        Assert.assertFalse("Paused works ever", instance.isPaused());
    }
}