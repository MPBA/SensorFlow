package eu.fbk.mpba.sensorsflows;

public interface InputGroup extends NamedPlugin {

    void onInputStart();

    void onInputStop();

    Iterable<Input> getChildren();

    String getSimpleName();

}
