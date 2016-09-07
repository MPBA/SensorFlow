package eu.fbk.mpba.sensorsflows.plugins.outputs.litix;

import android.os.SystemClock;

import java.util.Map;
import java.util.TreeMap;

public abstract class TextStatusUpdater {
    Map<String, Object> tsParams = new TreeMap<>();
    long tsLastUpd = 0;

    public void textStatusPut(String k, Object v) {
        Object x = tsParams.put(k, v);
        // if (x != null && x != v)
            if (SystemClock.elapsedRealtime() - tsLastUpd > 33) {
                tsLastUpd = SystemClock.elapsedRealtime();
                StringBuilder text = new StringBuilder();
                for (Map.Entry<String, Object> e : tsParams.entrySet())
                    text
                            .append(e.getKey())
                            .append(": \t")
                            .append(e.getValue())
                            .append('\n');
                updateTextStatus(text.toString());
            }
    }

    public abstract void updateTextStatus(String text);
}
