package eu.fbk.mpba.sensorsflows;

/**
 * This class adds internal support for the library data-paths.
 */
class InputManager {
    private Manager _manager = null;
    private Status _status = Status.NOT_INITIALIZED;
    private Input _input;

    InputManager(Input input, Manager manager) {
        _input = input;
        _manager = manager;
    }

    private void changeStatus(Status s) {
        if (_manager != null)
            _manager.inputStatusChanged(this, _status = s);
    }

    void initializeInput() {
        changeStatus(Status.INITIALIZING);
        _input.onInputStart();
        changeStatus(Status.INITIALIZED);
    }

    void finalizeInput() {
        changeStatus(Status.FINALIZING);
        _input.onInputStop();
        changeStatus(Status.FINALIZED);
    }

    // Getters

    Iterable<Flow> getFlows() {
        return _input.getFlows();
    }

    Status getStatus() {
        return _status;
    }

    Manager getManager() {
        return _manager;
    }

    Input getInput() {
        return _input;
    }

    public enum Status {
        NOT_INITIALIZED, INITIALIZING, INITIALIZED, FINALIZING, FINALIZED
    }
}
