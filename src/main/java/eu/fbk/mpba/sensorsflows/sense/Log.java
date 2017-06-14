package eu.fbk.mpba.sensorsflows.sense;

import java.util.Locale;

class Log {
    private String text;
    private int type;
    private String tag;

    public Log(String text) {
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

    public Log invoke() {
        // URL escape just the ':' char
        String[] tokens = text.split(":");
        type = -1;
        tag = "";
        if (tokens.length == 3) {
            type = Integer.getInteger(tokens[0]);
            tag = tokens[1];
            text = tokens[2];
        }
        return this;
    }

    static String format(int type, String tag, String message) {
        // URL escape just the ':' char
        return String.format(Locale.ENGLISH,
                "%d\t%s\t%s",
                type,
                tag.replace("\\", "\\\\").replace("\t", "\\t"),
                message
        );
    }
}
