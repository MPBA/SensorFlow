package eu.fbk.mpba.sensorflow.sense;

import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;

class LogInput extends Input {

    /**
     * Log that can be child of both Inputs and Outputs
     * @param name    The name, preferably of the parent of this Input.
     */
    LogInput(String name) {
        super(null, name, Collections.<String>emptyList());
    }

    public void pushLog(int type, String tag, String message) {
        super.pushLog(getTimeSource().getMonoUTCNanos(),
                LogMessage.format(type, tag, message)
        );
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onClose() {

    }
}
