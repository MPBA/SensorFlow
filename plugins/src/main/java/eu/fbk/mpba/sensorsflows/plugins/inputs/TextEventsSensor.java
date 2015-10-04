package eu.fbk.mpba.sensorsflows.plugins.inputs;

import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonoTimestampSource;

public class TextEventsSensor<ValueT> extends SensorComponent<Long, ValueT> {

    public TextEventsSensor(DevicePlugin<Long, ValueT> parent) {
        super(parent);
    }

    @Override
    public void switchOnAsync() {
        sensorEvent(((IMonoTimestampSource)getParentDevicePlugin()).getMonoUTCNanos(),
                1, "Switched on");
    }

    @Override
    public void switchOffAsync() {
        sensorEvent(((IMonoTimestampSource)getParentDevicePlugin()).getMonoUTCNanos(),
                2, "Switched off");
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Collections.emptyList();
    }

    public void addText(CharSequence text) {
        sensorEvent(((IMonoTimestampSource)getParentDevicePlugin()).getMonoUTCNanos(),
                0, text.toString());
    }

    public void addTimedText(long timestamp, CharSequence text) {
        sensorEvent(timestamp, 0, text.toString());
    }
}
