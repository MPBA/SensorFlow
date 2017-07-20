package eu.fbk.mpba.sensorsflows;

public interface InputGroup extends NamedPlugin {

    void onCreate();

    void onStart();

    void onStop();

    Iterable<Input> getChildren();

    String getSimpleName();

}
