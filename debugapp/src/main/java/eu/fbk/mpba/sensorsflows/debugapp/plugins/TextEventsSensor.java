package eu.fbk.mpba.sensorsflows.debugapp.plugins;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;

public class TextEventsSensor<ValueT> extends SensorComponent<Long, ValueT> {

    private final String name;

    protected TextEventsSensor(DevicePlugin<Long, ValueT> parent, String name) {
        super(parent);
        this.name = name;
    }

    @Override
    public void switchOnAsync() {
        sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                1, "Switched on");
    }

    @Override
    public void switchOffAsync() {
        sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                2, "Switched off");
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList();
    }

    public void addText(CharSequence text) {
        sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()),
                0, text.toString());
    }

    public void addTimedText(long timestamp, CharSequence text) {
        sensorEvent(timestamp, 0, text.toString());
    }

    @Override
    public String toString() {
        return (getParentDevicePlugin() != null ? getParentDevicePlugin().toString() + "/" : "") + "TextEvents-" + name;
    }
}
