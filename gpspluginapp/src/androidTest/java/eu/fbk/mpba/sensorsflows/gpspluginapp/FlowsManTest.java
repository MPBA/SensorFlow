package eu.fbk.mpba.sensorsflows.gpspluginapp;

import junit.framework.TestCase;

/**
 * This class provides base test methods for the calss FlowsMan
 */
public class FlowsManTest extends TestCase {
/*
    public void setUp() throws Exception {
        super.setUp();
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<>();

        Assert.assertEquals(" Once created the engine goes STANDBY",
                instance.getStatus(), EngineStatus.STANDBY);*/
    }/*

    public void tearDown() throws Exception {

    }

    public void testAddDevice() throws Exception {
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<>();

        DevicePlugInStub d = new DevicePlugInStub("Saxophone", 10);
        //Assert.assertEquals("There are no devices", instance._userDevices.size(), 0);
        instance.addDevice(d);
        //Assert.assertEquals("There is a device", instance._userDevices.size(), 1);
        DevicePlugInStub e = new DevicePlugInStub("Microphone", 3);
        instance.addDevice(e);
        //Assert.assertEquals("There are 2 devices", instance._userDevices.size(), 2);

        instance.start();

        DevicePlugInStub f = new DevicePlugInStub("Uphold", 3);
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
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<Long, float[]>();

        DevicePlugInStub d = new DevicePlugInStub("Saxophone", 10);
        DevicePlugInStub e = new DevicePlugInStub("Microphone", 3);
        DevicePlugInStub f = new DevicePlugInStub("Uphold", 3);
        instance.addDevice(d);
        instance.addDevice(e);
        instance.addDevice(f);
        OutputStub o = new OutputStub("Saxophone");
        OutputStub p = new OutputStub("Microphone");
        OutputStub q = new OutputStub("Uphold");
        instance.addOutput(o);
        instance.addOutput(p);
        instance.addOutput(q);

        for (SensorComponent<Long, float[]> s : d.getSensors()) {
            instance.addLink(s, o);
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            instance.addLink(s, p);
        }
        for (SensorComponent<Long, float[]> s : f.getSensors()) {
            instance.addLink(s, p);
        }

        for (SensorComponent<Long, float[]> s : d.getSensors()) {
            Assert.assertEquals("Number of linked outputs", 1, s.getOutputsCount());
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            Assert.assertEquals("Number of linked outputs", 1, s.getOutputsCount());
        }
        for (SensorComponent<Long, float[]> s : f.getSensors()) {
            Assert.assertEquals("Number of linked outputs", 1, s.getOutputsCount());
        }

        instance.start();

        Assert.assertEquals("Number of linked sensors", 10, o.getLinkedSensorsCount());
        Assert.assertEquals("Number of linked sensors", 6, p.getLinkedSensorsCount());
        Assert.assertEquals("Number of linked sensors", 0, q.getLinkedSensorsCount());
    }

    public void testAddOutput() throws Exception {
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<Long, float[]>();

        Assert.assertEquals("There are no outputs", 0, instance._userOutputs.size());
        OutputStub d = new OutputStub("Saxophone");
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
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<Long, float[]>();

        DevicePlugInStub d = new DevicePlugInStub("Saxophone", 10);
        DevicePlugInStub e = new DevicePlugInStub("Microphone", 3);
        DevicePlugInStub f = new DevicePlugInStub("Uphold", 3);
        instance.addDevice(d);
        instance.addDevice(e);
        instance.addDevice(f);

        int numba = 0;
        for (DevicePlugIn<Long, float[]> ignored : instance.getDevices()) {
            numba++;
        }

        Assert.assertEquals("There are three devices", numba, 3);

        instance.start();

        numba = 0;
        for (DevicePlugIn<Long, float[]> ignored : instance.getDevices()) {
            numba++;
        }

        Assert.assertEquals("There are three devices", numba, 3);
    }

    public void testGetOutputs() throws Exception {
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<Long, float[]>();

        OutputStub d = new OutputStub("Saxophone");
        OutputStub e = new OutputStub("Microphone");
        OutputStub f = new OutputStub("Uphold");
        instance.addOutput(d);
        instance.addOutput(e);
        instance.addOutput(f);

        int numba = 0;
        for (OutputPlugIn ignored : instance.getOutputs()) {
            numba++;
        }

        Assert.assertEquals("There are three outputs", 3, numba);

        instance.start();

        numba = 0;
        for (OutputPlugIn ignored : instance.getOutputs()) {
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

    public void testSwitchOn() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();
    }

    public void testSwitchOff() throws Exception {
        FlowsMan<Integer, Double> instance;
        instance = new FlowsMan<Integer, Double>();
    }

    public void testSetSensorListened() throws Exception {
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<Long, float[]>();

        DevicePlugInStub d = new DevicePlugInStub("Saxophone", 10);
        DevicePlugInStub e = new DevicePlugInStub("Microphone", 3);
        instance.addDevice(d);
        instance.addDevice(e);

        OutputStub o = new OutputStub("Saxophone");
        instance.addOutput(o);

        for (SensorComponent<Long, float[]> s : d.getSensors()) {
            instance.addLink(s, o);
            instance.setSensorListened(s, true);
            Assert.assertTrue("The sensor is listened", instance.isSensorListened(s));
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            instance.addLink(s, o);
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            instance.setSensorListened(s, true);
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            Assert.assertTrue("The sensor is now listened", instance.isSensorListened(s));
        }

        instance.start();

        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            instance.setSensorListened(s, false);
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            Assert.assertFalse("The sensor is now not listened", instance.isSensorListened(s));
        }
    }

    public void testIsSensorListened() throws Exception {
        FlowsMan<Long, float[]> instance;
        instance = new FlowsMan<Long, float[]>();

        OutputStub o = new OutputStub("Saxophone");
        instance.addOutput(o);

        DevicePlugInStub d = new DevicePlugInStub("Saxophone", 10);
        DevicePlugInStub e = new DevicePlugInStub("Microphone", 3);

        for (SensorComponent<Long, float[]> s : d.getSensors()) {
            boolean error = false;
            try {
                instance.setSensorListened(s, true);
            } catch (NoSuchElementException ex) {
                error = true;
            }
            Assert.assertTrue("Can't set a sensor listened before the device has been added", error);
        }

        instance.addDevice(d);
        instance.addDevice(e);

        for (SensorComponent<Long, float[]> s : d.getSensors()) {
            instance.addLink(s, o);
            Assert.assertFalse("The sensor is not listened at start", instance.isSensorListened(s));
            instance.setSensorListened(s, true);
            Assert.assertTrue("The sensor is listened", instance.isSensorListened(s));
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            instance.addLink(s, o);
            Assert.assertFalse("The sensor is not listened at the begin", instance.isSensorListened(s));
        }
        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            instance.setSensorListened(s, true);
        }

        instance.start();

        for (SensorComponent<Long, float[]> s : e.getSensors()) {
            Assert.assertTrue("The sensor is now listened", instance.isSensorListened(s));
        }
    }
}*/