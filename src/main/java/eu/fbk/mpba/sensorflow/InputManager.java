package eu.fbk.mpba.sensorflow;

class InputManager {
    private InputObserver manager = null;
    private PluginStatus status = PluginStatus.INSTANTIATED;
    private InputGroup inputGroup;

    InputManager(InputGroup inputGroup, InputObserver manager) {
        this.inputGroup = inputGroup;
        this.manager = manager;
        changeStatus(PluginStatus.INSTANTIATED);
    }

    private void changeStatus(PluginStatus s) {
        if (manager != null)
            manager.inputStatusChanged(this, status = s);
    }

    // Events

    void onCreateAndStart() {
        switch (status) {
            case INSTANTIATED:
                inputGroup.onCreate();
                changeStatus(PluginStatus.CREATED);
            case CREATED:
                inputGroup.onAdded();
                changeStatus(PluginStatus.STARTED);
                break;
            default:
                System.out.println("onCreateAndStart out of place 3290erj28, current status: " + status.toString());
        }
    }

    void onStopAndClose() {
        switch (status) {
            case STARTED:
                inputGroup.onRemoved();
                changeStatus(PluginStatus.STOPPED);
            case CREATED:
            case STOPPED:
                inputGroup.onClose();
                changeStatus(PluginStatus.CLOSED);
                break;
            default:
                System.out.println("onStopAndClose out of place 3290erj29, current status: " + status.toString());
        }
    }

    // Getters

    Iterable<Input> getInputs() {
        return inputGroup.getChildren();
    }

    PluginStatus getStatus() {
        return status;
    }

    InputGroup getInputGroup() {
        return inputGroup;
    }

}
