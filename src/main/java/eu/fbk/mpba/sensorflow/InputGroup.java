package eu.fbk.mpba.sensorflow;

public interface InputGroup extends NamedPlugin {

    void onCreate();

    void onStart();

    void onStop();

    Iterable<Input> getChildren();

    String getSimpleName();

}
