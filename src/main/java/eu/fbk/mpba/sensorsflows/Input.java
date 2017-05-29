package eu.fbk.mpba.sensorsflows;

public interface Input extends Plugin {

    void onInputStart();

    void onInputStop();

    Iterable<Flow> getFlows();

}
