package eu.fbk.mpba.sensorsflows.sense;

import eu.fbk.mpba.sensorsflows.Input;
import eu.fbk.mpba.sensorsflows.Plugin;

class PluginLogInput extends Input {
    @Override
    public void turnOn() {

    }

    @Override
    public void turnOff() {

    }

    public PluginLogInput(Plugin parent) {
        super(null, parent.getName());
    }

    public void pushLog(int type, String tag, String message) {
        super.pushLog(getTimeSource().getMonoUTCNanos(),
                Log.format(type, tag, message)
        );
    }
}
