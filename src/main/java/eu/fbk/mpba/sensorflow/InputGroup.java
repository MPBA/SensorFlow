package eu.fbk.mpba.sensorflow;

public interface InputGroup extends SFPlugin {

    void onCreate();

    void onAdded();

    void onRemoved();

    Iterable<Input> getChildren();

    String getSimpleName();

}
