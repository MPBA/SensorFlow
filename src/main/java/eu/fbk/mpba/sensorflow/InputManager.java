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

    void onCreate() {
        switch (status) {
            case INSTANTIATED:
                inputGroup.onCreate();
                changeStatus(PluginStatus.CREATED);
                break;
            default:
                throw new UnsupportedOperationException("onCreate out of place, current status: " + status.toString());
        }
    }

    void onAdded() {
        switch (status) {
            case CREATED:
                inputGroup.onAdded();
                changeStatus(PluginStatus.STARTED);
                break;
            default:
                throw new UnsupportedOperationException("onAdded out of place, current status: " + status.toString());
        }
    }

    void onRemovedAndClose() {
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
                throw new UnsupportedOperationException("onRemovedAndClose out of place, current status: " + status.toString());
        }
    }

    // Getters

    Iterable<Input> getInputs() {
        return inputGroup.getChildren();
    }

//    PluginStatus getStatus() {
//        return status;
//    }

    InputGroup getInputGroup() {
        return inputGroup;
    }

}
