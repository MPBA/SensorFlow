package eu.fbk.mpba.sensorsflows;

import java.util.List;

public interface Output extends Plugin {

    void onOutputStart(String sessionId, List<Flow> flows);

    void onOutputStop();

    void onLog(Flow flow, long timestamp, String message);

    void onValue(Flow flow, long timestamp, double[] value);

}
