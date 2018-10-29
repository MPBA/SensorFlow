package eu.fbk.mpba.sensorflow.chunks;

import eu.fbk.mpba.sensorflow.Input;

public interface ChunkCooker {
    void setTrackName(String trackName);

    void setFlushReason(FlushReason r);

    FlushReason getFlushReason();

    void setId(int i);

    int getId();

    void addInput(Input input);

    int addValue(Input flow, long time, double[] value);

    int addLog(Input flow, long time, String message);

    int getBegin();

    int getDuration();

    void setBegin(int begin);

    void setDuration(int duration);

    interface Factory {
        ChunkCooker newInstance();
    }
}
