package eu.fbk.mpba.sensorsflows;

import junit.framework.Assert;
import junit.framework.TestCase;

import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.stubs.DeviceStub;

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
        try {
            instance.addDevice(f);

        } catch (UnsupportedOperationException ex) {
            ex.printStackTrace();
            // TODO TEST_HEAD
        }
        Assert.assertEquals("There are 2 devices", instance._userDevices.size(), 2);
    }

    public void testAddLink() throws Exception {

    }

    public void testAddOutput() throws Exception {

    }

    public void testGetDevices() throws Exception {

    }

    public void testGetOutputs() throws Exception {

    }

    public void testIsPaused() throws Exception {

    }

    public void testSetPaused() throws Exception {

    }
}