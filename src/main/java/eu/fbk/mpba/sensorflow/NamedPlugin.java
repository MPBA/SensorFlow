package eu.fbk.mpba.sensorflow;

public interface NamedPlugin {
    String getName();
    void onClose();
}
