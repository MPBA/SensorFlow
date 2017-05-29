package eu.fbk.mpba.sensorsflows;

/**
 * This class adds internal support for the library data-paths.
 */
class InputManager {
    private SensorFlow _manager = null;
    private Input.Status _status = Input.Status.NOT_INITIALIZED;
    private Input _input;

    InputManager(Input input, SensorFlow manager) {
        _input = input;
        _manager = manager;
    }

    private void changeStatus(Input.Status s) {
        if (_manager != null)
            _manager.inputStatusChanged(this, _status = s);
    }

    void initializeInput() {
        changeStatus(Input.Status.INITIALIZING);
        _input.onInputStart();
        changeStatus(Input.Status.INITIALIZED);
    }

    void finalizeInput() {
        changeStatus(Input.Status.FINALIZING);
        _input.onInputStop();
        changeStatus(Input.Status.FINALIZED);
    }

    // Getters

    Iterable<Flow> getFlows() {
        return _input.getFlows();
    }

    Input.Status getStatus() {
        return _status;
    }

    SensorFlow getManager() {
        return _manager;
    }

    Input getInput() {
        return _input;
    }
}
