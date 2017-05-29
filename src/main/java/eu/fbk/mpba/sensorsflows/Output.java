package eu.fbk.mpba.sensorsflows;

import java.util.List;

public interface Output extends Plugin {

    void onOutputStart(Object sessionTag, List<Flow> streamingSensors);

    void onOutputStop();

    void onLog(Flow flow, long timestamp, int code, String message);

    void onValue(Flow flow, long timestamp, double[] value);

}
