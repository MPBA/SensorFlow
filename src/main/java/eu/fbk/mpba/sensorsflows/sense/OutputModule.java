package eu.fbk.mpba.sensorsflows.sense;

import java.util.List;

import eu.fbk.mpba.sensorsflows.Flow;
import eu.fbk.mpba.sensorsflows.Output;

public abstract class OutputModule extends Plugin implements Output {
    private boolean flowing = false;

    /**
     * Constructor of abstract class
     *
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public OutputModule(String settings) {
        super(settings);
    }

    @Override
    public void onOutputStart(String sessionTag, List<Flow> flows) {
        start(sessionTag, flows);
        flowing = true;
    }

    @Override
    public void onOutputStop() {
        stop();
        flowing = false;
    }

    /**
     * Is called for each Raw Log that reaches this OutputModule
     * @param flow
     * @param timestamp
     * @param text
     */
    @Override
    public void onLog(Flow flow, long timestamp, String text) {
        String[] tokens = text.split(":");
        int type = -1;
        String tag = "";
        if (tokens.length == 3) {
            type = Integer.getInteger(tokens[0]);
            tag = tokens[1];
            text = tokens[2];
        }
        onLog(flow, timestamp, type, tag, text);
    }

    /**
     * Is called for each Log that reaches this OutputModule, if the log is formatted, it will be
     * split in code, tag and message, otherwise all the raw text will be put in message and type
     * will be -1.
     *
     * To read raw log messages override the method onLog(Flow, long, String) instead of this.
     * @param flow
     * @param timestamp
     * @param type
     * @param tag
     * @param message
     */
    public abstract void onLog(Flow flow, long timestamp, int type, String tag, String message);

    /**
     * Is called for each Value that reaches this OutputModule
     * @param flow
     * @param timestamp
     * @param value
     */
    @Override
    public abstract void onValue(Flow flow, long timestamp, double[] value);

    /**
     * True when the acquisition is in progress i.e. between the start and stop calls. When it
     * returns true, some attributes of the InputModule can not be altered.
     *
     * @return True if it is flowing, false otherwise.
     */
    @Override
    public boolean isFlowing() {
        return flowing;
    }

    /**
     * This method is called when the acquisition is starting. After this method returns, data can
     * be pushed, and the method isFlowing returns true.
     * @param acquisitionId Identification of the acquisition that is being started.
     * @param flows Flows that will stream to this OutputModule
     */
    protected abstract void start(String acquisitionId, List<Flow> flows);
}
