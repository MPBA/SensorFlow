package eu.fbk.mpba.sensorflow.sense;

import java.util.Comparator;
import java.util.TreeSet;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;
import eu.fbk.mpba.sensorflow.Output;

public abstract class OutputModule extends Module implements Output, IOutputModule {

    /**
     * Constructor of abstract class
     *
     * @param name Name of the Module.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    protected OutputModule(String name, String settings) {
        setName(name);
        setConfiguration(settings);
    }

    /**
     * This method is called when a new log message is available to be used, transmitted or
     * persisted. This method can be overridden to skip the parsing of the log type and tag.
     * @param input Input that generated the log.
     * @param timestamp Timestamp of the generation of the message
     * @param text String encoding the type, the tag and the message.
     */
    @Override
    public void onLog(Input input, long timestamp, String text) {
        LogMessage l = new LogMessage(text).invoke();
        onLog(input, timestamp, l.getType(), l.getTag(), l.getText());
    }

    /**
     * This method is called when a new value vector is available to be used, transmitted or
     * persisted.
     * @param input Input that generated the data.
     * @param timestamp Generation timestamp of the data.
     * @param value The data.
     */
    @Override
    public abstract void onValue(Input input, long timestamp, double[] value);

    /**
     * This method is called when a new log message is available to be used, transmitted or
     * persisted.
     * @param input Input that generated the log.
     * @param timestamp Timestamp of the generation of the message
     * @param type Type of the message.
     * @param tag Tag associated with the message.
     * @param message the message.
     */
    @Override
    public abstract void onLog(Input input, long timestamp, int type, String tag, String message);

    @Override
    public abstract void onCreate(String sessionId);

    private final TreeSet<InputGroup> distinct = new TreeSet<>(new Comparator<InputGroup>() {
        @Override
        public int compare(InputGroup o1, InputGroup o2) {
            return o1.hashCode() - o2.hashCode();
        }
    });

    @Override
    public void onInputAdded(Input input) {
        InputGroup parent = input.getParent();
        if (parent != null
                && parent instanceof InputModule
                && distinct.add(parent)) {
            onInputParentAdded(parent);
        }
    }

    @Override
    public void onInputRemoved(Input input) {
        InputGroup parent = input.getParent();
        if (parent != null
                && parent instanceof InputModule
                && distinct.remove(parent)
                && !distinct.contains(parent)) {
            onInputParentRemoved(parent);
        }
    }

    protected void onInputParentAdded(InputGroup inputParent) { }

    protected void onInputParentRemoved(InputGroup inputParent) { }

    @Override
    public abstract void onClose();
}
