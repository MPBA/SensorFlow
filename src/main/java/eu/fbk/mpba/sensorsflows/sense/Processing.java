package eu.fbk.mpba.sensorsflows.sense;

import java.util.List;

import eu.fbk.mpba.sensorsflows.Flow;
import eu.fbk.mpba.sensorsflows.Output;

public class Processing extends InputModule implements Output {
    /**
     * Constructor of abstract class
     *
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public Processing(String settings) {
        super(settings);
    }

    @Override
    public void onOutputStart(String sessionId, List<Flow> flows) {

    }

    @Override
    public void onOutputStop() {

    }

    @Override
    public void onLog(Flow flow, long timestamp, String message) {

    }

    @Override
    public void onValue(Flow flow, long timestamp, double[] value) {

    }

    @Override
    protected void start() {

    }

    @Override
    protected void stop() {

    }

    @Override
    public void close() {

    }
}
