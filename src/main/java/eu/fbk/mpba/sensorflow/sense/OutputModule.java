package eu.fbk.mpba.sensorflow.sense;

import java.util.Comparator;
import java.util.TreeSet;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;
import eu.fbk.mpba.sensorflow.Output;

/**
 * This is the base class to implement an output plugin.
 */
public abstract class OutputModule extends Module implements Output, IOutputModule {

    /**
     * Constructor of abstract class
     *
     * @param name     Name of the Output.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    protected OutputModule(String name, String settings) {
        setName(name);
        setConfiguration(settings);
    }

    /**
     * This method is called when a new log message is available. This method can be overridden to
     * skip the parsing of the log type and tag.
     *
     * @param input     Input that generated the log.
     * @param timestamp Timestamp of the generation of the message
     * @param text      String encoding the type, the tag and the message.
     */
    @Override
    public void onLog(Input input, long timestamp, String text) {
        LogMessage l = new LogMessage(text).invoke();
        onLog(input, timestamp, l.getType(), l.getTag(), l.getText());
    }

    /**
     * This method is called when a new value vector is available.
     *
     * @param input     Input that generated the data.
     * @param timestamp Generation timestamp of the data.
     * @param value     The data.
     */
    @Override
    public abstract void onValue(Input input, long timestamp, double[] value);

    /**
     * This method is called when a new log message is available.
     *
     * @param input     Input that generated the log.
     * @param timestamp Timestamp of the generation of the message
     * @param type      Type of the message.
     * @param tag       Tag associated with the message.
     * @param message   the message.
     */
    @Override
    public abstract void onLog(Input input, long timestamp, int type, String tag, String message);

    /**
     * When overridden, is called to notify the creation of a new SensorFlow session.
     *
     * @param sessionId The name of the SensorFlow session.
     */
    @Override
    public abstract void onCreate(String sessionId);

    private final TreeSet<InputGroup> distinct = new TreeSet<>(new Comparator<InputGroup>() {
        @Override
        public int compare(InputGroup o1, InputGroup o2) {
            return o1.hashCode() - o2.hashCode();
        }
    });

    /**
     * Notifies a new Input that has been added.
     * Call super when overriding to make {@literal onInputModuleAdded} work.
     *
     * @param input The input that has been added.
     */
    @Override
    public void onInputAdded(Input input) {
        InputGroup parent = input.getParent();
        if (parent instanceof InputModule
                && distinct.add(parent)) {
            onInputModuleAdded((InputModule)parent);
        }
    }

    /**
     * Notifies that a former added input has been removed. This method is called also when a route
     * to this output was removed.
     * Call super when overriding to make {@literal onInputModuleRemoved} work.
     *
     * @param input The input that has been removed.
     */
    @Override
    public void onInputRemoved(Input input) {
        InputGroup parent = input.getParent();
        if (parent instanceof InputModule
                && distinct.remove(parent)
                && !distinct.contains(parent)) {
            onInputModuleRemoved((InputModule)parent);
        }
    }

    /**
     * This is called when an input module is added.
     *
     * @param inputParent The InputModule or ProcessingModule that has been added.
     */
    protected void onInputModuleAdded(InputModule inputParent) {
    }

    /**
     * This is called when an input module has been removed. This method is called also when a route
     * to this output was removed.
     *
     * @param inputParent The InputModule or ProcessingModule that has been removed.
     */
    protected void onInputModuleRemoved(InputModule inputParent) {
    }

    @Override
    public abstract void onClose();
}
