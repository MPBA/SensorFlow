package eu.fbk.mpba.sensorsflows;

/**
 * Main interface for the device's data transport.
 */
interface InputObserver {
    void inputStatusChanged(InputManager input, InputStatus state);
}
