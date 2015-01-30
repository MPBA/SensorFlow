package eu.fbk.mpba.sensorsflows.gpspluginapp;

import junit.framework.TestCase;

/**
 * This class provides stress test methods. The operation lasts 30s.
 */
public class StressTests extends TestCase {
/*
    public StressTests() {*/
    }/*

    FlowsMan<Long, float[]> m;

    public void testStrongMonotonicity() throws Exception {
        m = new FlowsMan<>();
        m.addDevice(new TestDevicePlugIn("I"));
        CsvOutput o;
        m.addOutput(o = new CsvOutput("O"));
        m.setAutoLinkMode(AutoLinkMode.NTH_TO_NTH);
        Assert.assertEquals("After creation it should be in standby.",
                EngineStatus.STANDBY, m.getStatus());
        m.start();
        Assert.assertTrue("After start it should be preparing or streaming.",
                EngineStatus.STREAMING == m.getStatus() || EngineStatus.PREPARING == m.getStatus());
        Thread.sleep(1000);
        Assert.assertEquals("After 1000ms it should be streaming.",
                EngineStatus.STREAMING, m.getStatus());
        Thread.sleep(30000);
        m.close();
        List<String> a = o.getFiles();
        for (String n : a) {
            Assert.assertTrue(CsvCheck.checkStrongMonotonicityOfTheFirstColumnLong(n));
        }
    }

    public void testParallelStress() throws Exception {
        m = new FlowsMan<>();
        m.addDevice(new TestDevicePlugIn("a1"));
        m.addOutput(new CsvOutput("a2"));
        m.addDevice(new TestDevicePlugIn("a3"));
        m.addOutput(new CsvOutput("a4"));
        m.addDevice(new TestDevicePlugIn("a5"));
        m.addOutput(new CsvOutput("a6"));
        m.addDevice(new TestDevicePlugIn("a7"));
        m.addOutput(new CsvOutput("a8"));
        m.addDevice(new TestDevicePlugIn("a9"));
        m.addOutput(new CsvOutput("a0"));
        m.addDevice(new TestDevicePlugIn("b1"));
        m.addOutput(new CsvOutput("b2"));
        m.addDevice(new TestDevicePlugIn("b3"));
        m.addOutput(new CsvOutput("b4"));
        m.addDevice(new TestDevicePlugIn("b5"));
        m.addOutput(new CsvOutput("b6"));
        m.addDevice(new TestDevicePlugIn("b7"));
        m.addOutput(new CsvOutput("b8"));
        m.addDevice(new TestDevicePlugIn("b9"));
        m.addOutput(new CsvOutput("b0"));
        m.setAutoLinkMode(AutoLinkMode.NTH_TO_NTH);
        Assert.assertEquals("After creation it should be in standby.",
                EngineStatus.STANDBY, m.getStatus());
        m.start();
        Assert.assertTrue("After start it should be preparing or streaming.",
                EngineStatus.STREAMING == m.getStatus() || EngineStatus.PREPARING == m.getStatus());
        Thread.sleep(1000);
        Assert.assertEquals("After 1000ms it should be streaming.",
                EngineStatus.STREAMING, m.getStatus());
        Thread.sleep(30000);
        m.close();
    }

    public void testFlowStress() throws Exception {
        m = new FlowsMan<>();
        m.addDevice(new TestDevicePlugIn("a1"));
        m.addDevice(new TestDevicePlugIn("a2"));
        m.addDevice(new TestDevicePlugIn("a3"));
        m.addDevice(new TestDevicePlugIn("a4"));
        m.addDevice(new TestDevicePlugIn("a5"));
        m.addDevice(new TestDevicePlugIn("b6"));
        m.addDevice(new TestDevicePlugIn("b7"));
        m.addDevice(new TestDevicePlugIn("b8"));
        m.addDevice(new TestDevicePlugIn("b9"));
        m.addDevice(new TestDevicePlugIn("b0"));
        m.addOutput(new CsvOutput("O"));
        m.setAutoLinkMode(AutoLinkMode.PRODUCT);
        Assert.assertEquals("After creation it should be in standby.",
                EngineStatus.STANDBY, m.getStatus());
        m.start();
        Assert.assertTrue("After start it should be preparing or streaming.",
                EngineStatus.STREAMING == m.getStatus() || EngineStatus.PREPARING == m.getStatus());
        Thread.sleep(1000);
        Assert.assertEquals("After 1000ms it should be streaming.",
                EngineStatus.STREAMING, m.getStatus());
        Thread.sleep(30000);
        m.close();
    }
}
*/