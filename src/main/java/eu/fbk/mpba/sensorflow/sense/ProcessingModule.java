package eu.fbk.mpba.sensorflow.sense;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;
import eu.fbk.mpba.sensorflow.Output;

/**
 * This is the base class to implement a processing plugin
 */
public abstract class ProcessingModule extends InputModule implements Output, IOutputModule {

    private final AtomicBoolean created = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public final void onCreate(String sessionId) {
        onCreate();
    }

    @Override
    public final void onCreate() {
        if (!created.getAndSet(true))
            onProcessingCreate();
    }

    @Override
    public final void onClose() {
        if (!closed.getAndSet(true))
            onProcessingClose();
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
     * This method is called when a new log is available. This method can be overridden to skip the
     * parsing of the log type and tag.
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
     * Called when the Processing plugin can be initialized.
     * Note that although a processor plugin is both input and output, this method is called just
     * once.
     */
    public abstract void onProcessingCreate();

    /**
     * Called after this plugin is added to SensorFlow as an input plugin.
     */
    @Override
    public abstract void onAdded();

    /**
     * Called after this plugin is added to SensorFlow as an input plugin.
     */
    @Override
    public abstract void onRemoved();

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

    /**
     * Called when the lifecycle of this plugin ends and the resources have to be freed.
     */
    public abstract void onProcessingClose();
}
