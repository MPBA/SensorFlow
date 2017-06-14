package eu.fbk.mpba.sensorsflows.sense;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import eu.fbk.mpba.sensorsflows.Input;
import eu.fbk.mpba.sensorsflows.InputGroup;


public class Stream extends Input {
    private String[] header;
    private String name;
    private boolean on;

    Stream(String header, String name) {
        this(null, header, name);
    }

    Stream(InputGroup parent, String header, String name) {
        this(parent, new String[] { header }, name);
    }

    Stream(String[] header, String name) {
        super(null);
        this.header = header;
        this.name = name;
    }

    Stream(InputGroup parent, String[] header, String name) {
        super(parent);
        this.header = header;
        this.name = name;
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
     * Raw log now.
     * @param message String containing the raw log text
     */
    public void pushLog(String message) {
        pushLog(getTimeSource().getMonoUTCMillis(), message);
    }

    /**
     * Formatted log now.
     * @param type Identification code of the log type
     * @param tag Tag for the log, can be seen as a sub-type or can be ignored.
     * @param message String containing the log message
     */
    public void pushLog(int type, String tag, String message) {
        pushLog(getTimeSource().getMonoUTCMillis(), type, tag, message);
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
                Log.format(type, tag, message)
        );
    }


    // May not be for the end developer

    @Override
    public Collection<String> getHeader() {
        return Arrays.asList(header);
    }

    @Override
    public String getSimpleName() {
        return name;
    }


    // Maybe useless still inherited

    public boolean isOn() {
        return on;
    }

    @Override
    public void turnOn() {
        on = true;
    }

    @Override
    public void turnOff() {
        on = false;
    }
}
