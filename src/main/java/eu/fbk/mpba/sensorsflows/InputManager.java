package eu.fbk.mpba.sensorsflows;

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
            case CREATED:
                inputGroup.onStart();
                changeStatus(PluginStatus.RESUMED);
                break;
            default:
                System.out.println("onCreate out of place 3290erj28, current status: " + status.toString());
        }
    }

    void onClose() {
        switch (status) {
            case RESUMED:
                inputGroup.onStop();
                changeStatus(PluginStatus.PAUSED);
            case CREATED:
            case PAUSED:
                inputGroup.onClose();
                changeStatus(PluginStatus.CLOSED);
                break;
            default:
                System.out.println("onClose out of place 3290erj29, current status: " + status.toString());
        }
    }

    // Getters

    Iterable<Input> getFlows() {
        return inputGroup.getChildren();
    }

    PluginStatus getStatus() {
        return status;
    }

    InputGroup getInput() {
        return inputGroup;
    }

}
