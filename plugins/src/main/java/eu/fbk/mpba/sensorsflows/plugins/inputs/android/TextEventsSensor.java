package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class TextEventsSensor<ValueT> extends SensorComponent<Long, ValueT> {

    public TextEventsSensor(DevicePlugin<Long, ValueT> parent) {
        super(parent);
    }

    @Override
    public void switchOnAsync() {
        sensorEvent(getTime().getMonoUTCNanos(),
                1, "Switched on");
    }

    @Override
    public void switchOffAsync() {
        sensorEvent(getTime().getMonoUTCNanos(),
                2, "Switched off");
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Collections.emptyList();
    }

    private long lastNoteTS = 0;

    public void addText(CharSequence text) {
        addTimedText(getTime().getMonoUTCNanos(), text.toString());
    }

    public void addTimedText(long timestamp, CharSequence text) {
        sensorEvent(timestamp, (lastNoteTS < timestamp) ? 100 : 101, text.toString());
        lastNoteTS = timestamp;
    }
}
