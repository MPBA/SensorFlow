package eu.fbk.mpba.sensorsflows;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;

import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.stubs.CsvOutput;
import eu.fbk.mpba.sensorsflows.stubs.TestDevice;

/**
 * This class provides stress test methods. The operation lasts 30s.
 */
public class StressTests extends TestCase {

    public StressTests() {
    }

    FlowsMan<Long, float[]> m;

    public void testStrongMonotonicity() throws Exception {
        m = new FlowsMan<Long, float[]>();
        m.addDevice(new TestDevice("I"));
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
        m = new FlowsMan<Long, float[]>();
        m.addDevice(new TestDevice("a1"));
        m.addOutput(new CsvOutput("a2"));
        m.addDevice(new TestDevice("a3"));
        m.addOutput(new CsvOutput("a4"));
        m.addDevice(new TestDevice("a5"));
        m.addOutput(new CsvOutput("a6"));
        m.addDevice(new TestDevice("a7"));
        m.addOutput(new CsvOutput("a8"));
        m.addDevice(new TestDevice("a9"));
        m.addOutput(new CsvOutput("a0"));
        m.addDevice(new TestDevice("b1"));
        m.addOutput(new CsvOutput("b2"));
        m.addDevice(new TestDevice("b3"));
        m.addOutput(new CsvOutput("b4"));
        m.addDevice(new TestDevice("b5"));
        m.addOutput(new CsvOutput("b6"));
        m.addDevice(new TestDevice("b7"));
        m.addOutput(new CsvOutput("b8"));
        m.addDevice(new TestDevice("b9"));
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
        m = new FlowsMan<Long, float[]>();
        m.addDevice(new TestDevice("a1"));
        m.addDevice(new TestDevice("a2"));
        m.addDevice(new TestDevice("a3"));
        m.addDevice(new TestDevice("a4"));
        m.addDevice(new TestDevice("a5"));
        m.addDevice(new TestDevice("b6"));
        m.addDevice(new TestDevice("b7"));
        m.addDevice(new TestDevice("b8"));
        m.addDevice(new TestDevice("b9"));
        m.addDevice(new TestDevice("b0"));
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
