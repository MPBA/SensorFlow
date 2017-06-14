package eu.fbk.mpba.sensorsflows;

public interface InputGroup extends Plugin {

    void onInputStart();

    void onInputStop();

    Iterable<Input> getChildren();

    String getSimpleName();

}
