package eu.fbk.mpba.sensorsflows;

/**
 * This class adds internal support for the library data-paths.
 */
class InputManager {
    private Manager _manager = null;
    private Status _status = Status.NOT_INITIALIZED;
    private InputGroup _inputGroup;

    InputManager(InputGroup inputGroup, Manager manager) {
        _inputGroup = inputGroup;
        _manager = manager;
    }

    private void changeStatus(Status s) {
        if (_manager != null)
            _manager.inputStatusChanged(this, _status = s);
    }

    void initializeInput() {
        changeStatus(Status.INITIALIZING);
        _inputGroup.onInputStart();
        changeStatus(Status.INITIALIZED);
    }

    void finalizeInput() {
        changeStatus(Status.FINALIZING);
        _inputGroup.onInputStop();
        changeStatus(Status.FINALIZED);
    }

    // Getters

    Iterable<Input> getFlows() {
        return _inputGroup.getChildren();
    }

    Status getStatus() {
        return _status;
    }

    Manager getManager() {
        return _manager;
    }

    InputGroup getInput() {
        return _inputGroup;
    }

    public enum Status {
        NOT_INITIALIZED, INITIALIZING, INITIALIZED, FINALIZING, FINALIZED
    }
}
