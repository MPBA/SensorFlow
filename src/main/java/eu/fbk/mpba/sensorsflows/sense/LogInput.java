package eu.fbk.mpba.sensorsflows.sense;

import eu.fbk.mpba.sensorsflows.Input;
import eu.fbk.mpba.sensorsflows.NamedPlugin;

class LogInput extends Input {

    /**
     * Log that can be child of both Inputs and Outputs
     * @param parent    The NamedPlugin parent of this Input.
     */
    LogInput(NamedPlugin parent) {
        super(null, parent.getName());
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
