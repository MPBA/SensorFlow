package eu.fbk.mpba.sensorflow;

public interface Output extends SFPlugin {

    void onCreate(String sessionId);

    void onInputAdded(Input input);

    void onInputRemoved(Input input);

    /**
     * This method is called when a new value vector is available to be used, transmitted or
     * persisted.
     * @param input Input that generated the data.
     * @param timestamp Generation timestamp of the data.
     * @param value The data.
     */
    void onValue(Input input, long timestamp, double[] value);

    /**
     * This method is called when a new log message is available to be used, transmitted or
     * persisted. This method can be overridden to skip the parsing of the log type and tag.
     * @param input Input that generated the log.
     * @param timestamp Timestamp of the generation of the message
     * @param text String encoding the type, the tag and the message.
     */
    void onLog(Input input, long timestamp, String text);
}
