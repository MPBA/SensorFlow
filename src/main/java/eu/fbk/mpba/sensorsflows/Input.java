package eu.fbk.mpba.sensorsflows;

public interface Input extends Plugin {

    void onInputStart();

    void onInputStop();

    Iterable<Flow> getFlows();

    enum Status {
        NOT_INITIALIZED, INITIALIZING, INITIALIZED, FINALIZING, FINALIZED
    }
}
