package eu.fbk.mpba.sensorsflows.plugins.inputs.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.NodePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public class LogSensor<ValueT> extends SensorComponent<Long, ValueT> {

    public LogSensor(NodePlugin<Long, ValueT> parent) {
        super(parent);
    }

    private List<SensorEventEntry<Long>> supaBuffer = new ArrayList<>();
    private boolean buffer = true;

    @Override
    public void sensorEvent(Long time, int type, String message) {
        if (buffer)
            supaBuffer.add(new SensorEventEntry<>(this, time, type, message));
        else
            super.sensorEvent(time, type, message);
    }

    @Override
    public void switchOnAsync() {
        buffer = false;
        for (SensorEventEntry<Long> i : supaBuffer)
            sensorEvent(i.timestamp, i.code, i.message);

        sensorEvent(getTime().getMonoUTCNanos(),
                1, "Switched on");
    }

    @Override
    public void switchOffAsync() {
        sensorEvent(getTime().getMonoUTCNanos(),
                2, "Switched off");

        buffer = true;
    }

    @Override
    public List<Object> getValueDescriptor() {
        return Collections.emptyList();
    }

    private long lastNoteTS = 0;

    public void addText(CharSequence text) {
        addTimedText(getTime().getMonoUTCNanos(), text.toString());
    }

    // FIXME non monotonic, 101 means an out of time note!!!!!!!!!!!
    public void addTimedText(long timestamp, CharSequence text) {
        sensorEvent(timestamp, (lastNoteTS < timestamp) ? 100 : 101, text.toString());
        lastNoteTS = timestamp;
    }

    public void addLog(int code, CharSequence text) {
        if (code >= 100000)
            throw new IllegalArgumentException("0-99999 codes supported for addLog");
        sensorEvent(getTime().getMonoUTCNanos(), code + 100000, text.toString());
        lastNoteTS = getTime().getMonoUTCNanos();
    }

    public void addMeta(int code, CharSequence text) {
        if (code >= 100000)
            throw new IllegalArgumentException("0-99999 codes supported for addMeta");
        sensorEvent(getTime().getMonoUTCNanos(), code + 200000, text.toString());
        lastNoteTS = getTime().getMonoUTCNanos();
    }
}
