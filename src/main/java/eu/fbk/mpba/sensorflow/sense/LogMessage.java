package eu.fbk.mpba.sensorflow.sense;

import java.util.Locale;

class LogMessage {
    private String text;
    private int type;
    private String tag;

    public LogMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public String getTag() {
        return tag;
    }

    public LogMessage invoke() {
        // URL escape just the '\t' char
        String[] tokens = text.split("\t");
        type = -1;
        tag = "";
        if (tokens.length > 1) {
            type = Integer.decode(tokens[0]);
            tag = tokens[1];
            if (tokens.length == 3)
                text = tokens[2];
        }

        return this;
    }

    static String format(int type, String tag, String message) {
        // URL escape just the '\t' char
        return String.format(Locale.ENGLISH,
                "%d\t%s\t%s",
                type,
                tag.replace("\\", "\\\\").replace("\t", "\\t"),
                message
        );
    }
}
