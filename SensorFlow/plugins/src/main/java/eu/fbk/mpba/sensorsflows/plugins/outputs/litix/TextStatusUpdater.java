package eu.fbk.mpba.sensorsflows.plugins.outputs.litix;

import java.util.Map;
import java.util.TreeMap;

public abstract class TextStatusUpdater {
    private Map<String, Object> tsParams = new TreeMap<>();

    public void put(String k, Object v) {
        tsParams.put(k, v);
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, Object> e : tsParams.entrySet())
            text.append(e.getKey())
                .append(": \t")
                .append(e.getValue())
                .append('\n');
        updateTextStatus(text.toString());
    }

    public Object remove(String k) {
        return tsParams.remove(k);
    }

    public abstract void updateTextStatus(String text);
}
