package eu.fbk.mpba.sensorsflows;

import android.test.InstrumentationTestCase;

import eu.fbk.mpba.sensorsflows.stubs.CsvOutput;
import eu.fbk.mpba.sensorsflows.stubs.SmartphoneDevice;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends InstrumentationTestCase {
    FlowsMan<Long, float[]> m;

    public ApplicationTest() {
        //super(Application.class);
        // m = new FlowsMan<Long, float[]>();
        m.addDevice(new SmartphoneDevice(getInstrumentation().getContext()));
        m.addOutput(new CsvOutput());
        m.setAutoLinkMode(AutoLinkMode.NTH_TO_NTH);
        m.start();
    }
}