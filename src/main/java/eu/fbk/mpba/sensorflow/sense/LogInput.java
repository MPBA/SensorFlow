package eu.fbk.mpba.sensorflow.sense;

import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.NamedPlugin;

class LogInput extends Input {

    /**
     * Log that can be child of both Inputs and Outputs
     * @param parent    The NamedPlugin parent of this Input.
     */
    LogInput(NamedPlugin parent) {
        super(null, parent.getName(), Collections.emptyList());
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
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onClose() {

    }
}
