package eu.fbk.mpba.sensorflow.sense;

import eu.fbk.mpba.sensorflow.Input;

interface IOutputModule {
    /**
     * This method is called when a new log message is available to be used, transmitted or
     * persisted.
     * If the log is formatted, it will be split in code, tag and message, otherwise all the raw
     * text will be put in message and type set to -1.
     * @param input Input that generated the log.
     * @param timestamp Timestamp of the generation of the message
     * @param type Type of the message.
     * @param tag Tag associated with the message.
     * @param message the message.
     */
    void onLog(Input input, long timestamp, int type, String tag, String message);
}
