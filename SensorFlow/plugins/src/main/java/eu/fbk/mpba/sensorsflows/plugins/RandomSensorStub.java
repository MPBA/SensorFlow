package eu.fbk.mpba.sensorsflows.plugins;

import android.os.SystemClock;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * Random values generator.
 *
 * Emulates a sensor that gives random values with avg = 0 and sd = pi/2
 */
public class RandomSensorStub extends SensorComponent<Long, float[]> {

    private String _name;
    private volatile boolean _streaming = false;

    public RandomSensorStub(NodePlugin<Long, float[]> d) {
        super(d);
        _name = "RandomSensor";
        final RandomSensorStub t = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean loop = true;
                while (loop) {
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        loop = false;
                    }
                    if (_streaming)
                        t.eventValue(new float[]
                                {       (float)(Math.random() * 3.14 - 1.57),
                                        (float)(Math.random() * 3.14 - 1.57),
                                        (float)(Math.random() * 3.14 - 1.57) });
                }
            }
        }, "PushThread:" + toString()).start();
    }

    private void eventValue(float[] v) {
        sensorValue(System.currentTimeMillis(), v);
    }

    @Override
    public void switchOnAsync() {
        if (getStatus() == SensorStatus.OFF) {
            _streaming = true;
            changeStatus(SensorStatus.ON);
            sensorEvent(System.currentTimeMillis(), 0, _name + " switched on");
        }
    }

    @Override
    public void switchOffAsync() {
        if (getStatus() == SensorStatus.ON) {
            _streaming = false;
            changeStatus(SensorStatus.OFF);
            sensorEvent(System.currentTimeMillis(), 0, _name + " switched off");
        }
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Arrays.asList((Object)"RandX", "RandY", "RandZ");
    }

    @Override
    public String getName() {
        return (getParentDevicePlugin() != null ? getParentDevicePlugin().toString() + "/" : "") + "/" + _name;
    }
}
