package eu.fbk.mpba.sensorsflows;

import java.util.List;

public interface Output extends Plugin {

    void onOutputStart(Object sessionTag, List<Flow> streamingSensors);

    void onOutputStop();

    void onEvent(Flow flow, long timestamp, int code, String message);

    void onValue(Flow flow, long timestamp, double[] value);

    enum Status {
        NOT_INITIALIZED, INITIALIZING, INITIALIZED, FINALIZING, FINALIZED
    }
}
