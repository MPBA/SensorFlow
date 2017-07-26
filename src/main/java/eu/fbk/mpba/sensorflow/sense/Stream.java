package eu.fbk.mpba.sensorflow.sense;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;

public class Stream extends Input {

    public static final Collection<String> HEADER_XYZ = Arrays.asList("x", "y", "z");
    public static final Collection<String> HEADER_VALUE = Collections.singletonList("value");

    private boolean on;

    public Stream(String header, String name) {
        this(null, header, name);
    }

    public Stream(InputGroup parent, String header, String name) {
        this(parent, new String[] { header }, name);
    }

    public Stream(String[] header, String name) {
        this(null, header, name);
    }

    public Stream(InputGroup parent, String[] header, String name) {
        this(parent, Arrays.asList(header), name);
    }

    public Stream(Collection<String> header, String name) {
        this(null, header, name);
    }

    public Stream(InputGroup parent, Collection<String> header, String name) {
        super(parent, name, header);
    }

    public void pushValue(double value) {
        super.pushValue(getTimeSource().getMonoUTCNanos(), new double[] { value });
    }

    /**
     * Logs the quality of the signal with the synchronous time reference.
     * @param timestamp Time reference to the signal.
     * @param quality InputModule-dependent codification of the quality.
     */
    public void pushQuality(long timestamp, String quality) {
        pushLog(timestamp, 0, "quality", quality);
    }

    /**
     * Logs now the quality of the signal with the synchronous time reference.
     * @param quality InputModule-dependent codification of the quality.
     */
    public void pushQuality(String quality) {
        pushLog(getTimeSource().getMonoUTCNanos(), 0, "quality", quality);
    }

    /**
     * Formatted log.
     * @param timestamp Time reference of this log line.
     * @param type Identification code of the log type
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    public void pushLog(long timestamp, int type, String tag, String message) {
        // URL escape just the ':' char
        super.pushLog(timestamp,
                LogMessage.format(type, tag, message)
        );
    }

    /**
     * Raw log now.
     * @param message String containing the raw log text
     */
    public void pushLog(String message) {
        pushLog(getTimeSource().getMonoUTCNanos(), message);
    }

    /**
     * Formatted log now.
     * @param type Identification code of the log type
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    public void pushLog(int type, String tag, String message) {
        pushLog(getTimeSource().getMonoUTCNanos(), type, tag, message);
    }

    // May not be for the end developer

    @Override
    public void onCreate() {

    }

    @Override
    public void onAdded() {
        on = true;
    }

    @Override
    public void onRemoved() {
        on = false;
    }

    @Override
    public void onClose() {

    }

    // Maybe useless still inherited

    public boolean isOn() {
        return on;
    }
}
