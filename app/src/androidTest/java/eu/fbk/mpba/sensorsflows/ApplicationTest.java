package eu.fbk.mpba.sensorsflows;

import android.app.Application;
import android.test.ApplicationTestCase;

import eu.fbk.mpba.sensorsflows.stubs.CsvOutput;
import eu.fbk.mpba.sensorsflows.stubs.SmartphoneDevice;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        FlowsMan<Long, float[]> m = new FlowsMan<Long, float[]>();
        m.addDevice(new SmartphoneDevice(getContext()));
        m.addOutput(new CsvOutput());
    }
}