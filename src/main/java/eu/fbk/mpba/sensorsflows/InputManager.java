package eu.fbk.mpba.sensorsflows;

/**
 * This class adds internal support for the library data-paths.
 */
class InputManager {
    private SensorFlow _manager = null;
    private InputStatus _status = InputStatus.NOT_INITIALIZED;
    private Input _input;

    InputManager(Input input, SensorFlow manager) {
        _input = input;
        _manager = manager;
    }

    private void changeStatus(InputStatus s) {
        if (_manager != null)
            _manager.inputStatusChanged(this, _status = s);
    }

    void initializeInput() {
        changeStatus(InputStatus.INITIALIZING);
        _input.onInputStart();
        changeStatus(InputStatus.INITIALIZED);
    }

    void finalizeInput() {
        changeStatus(InputStatus.FINALIZING);
        _input.onInputStop();
        changeStatus(InputStatus.FINALIZED);
    }

    // Getters

    Iterable<Flow> getFlows() {
        return _input.getFlows();
    }

    InputStatus getStatus() {
        return _status;
    }

    SensorFlow getManager() {
        return _manager;
    }

    Input getInput() {
        return _input;
    }
}
