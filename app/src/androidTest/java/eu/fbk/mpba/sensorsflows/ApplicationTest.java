package eu.fbk.mpba.sensorsflows;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.stubs.CsvOutput;
import eu.fbk.mpba.sensorsflows.stubs.SmartphoneDevice;

public class ApplicationTest extends InstrumentationTestCase {

    public ApplicationTest() {
    }

    FlowsMan<Long, float[]> m;

    public void testOperation() throws Exception {
        m = new FlowsMan<Long, float[]>();
        m.addDevice(new SmartphoneDevice());
        m.addOutput(new CsvOutput());
        m.setAutoLinkMode(AutoLinkMode.NTH_TO_NTH);
        Assert.assertEquals("After creation it should be in standby.",
                EngineStatus.STANDBY, m.getStatus());
        m.start();
        Assert.assertTrue("After start it should be preparing or streaming.",
                EngineStatus.STREAMING == m.getStatus() || EngineStatus.PREPARING == m.getStatus());
        Thread.sleep(1000);
        //Assert.assertEquals("After 1000ms it should be streaming.",
        //        EngineStatus.STREAMING, m.getStatus());
        Thread.sleep(1000000);
        m.close();
    }
}
