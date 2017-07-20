package eu.fbk.mpba.sensorsflows.sense;

import java.util.List;

import eu.fbk.mpba.sensorsflows.Input;
import eu.fbk.mpba.sensorsflows.Output;

public abstract class OutputModule extends Module implements Output, IOutputModule {
    private boolean flowing = false;

    /**
     * Constructor of abstract class
     *
     * @param name Name of the Module.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public OutputModule(String name, String settings) {
        super(name, settings);
        addSFChild(this);
    }

    @Override
    public final void onInputAdded(String sessionId, List<Input> inputList) {
        flowing = true;
        start(sessionId, inputList);
    }

    @Override
    public final void onInputRemoved() {
        flowing = false;
        stop();
    }

    @Override
    public final boolean isFlowing() {
        return flowing;
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

    /**
     * This method is called when the acquisition is beginning. After this method returns, data may
     * be pushed, and the method isFlowing returns true.
     * @param acquisitionId Identification of the acquisition that is being started.
     * @param inputs Inputs that will stream to this OutputModule
     */
    protected abstract void start(String acquisitionId, List<Input> inputs);
}
