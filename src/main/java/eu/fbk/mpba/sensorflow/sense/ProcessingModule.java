package eu.fbk.mpba.sensorflow.sense;

import java.util.concurrent.atomic.AtomicBoolean;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.Output;

public abstract class ProcessingModule extends InputModule implements Output, IOutputModule {

    private final AtomicBoolean created = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructor of abstract class
     *
     * @param name     Name of the Module.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public ProcessingModule(String name, String settings) {
        super(name, settings);
    }

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

    public abstract void onProcessingCreate();

    @Override
    public abstract void onAdded();

    @Override
    public abstract void onRemoved();

    @Override
    public abstract void onInputAdded(Input input);

    @Override
    public abstract void onInputRemoved(Input input);

    public abstract void onProcessingClose();
}
