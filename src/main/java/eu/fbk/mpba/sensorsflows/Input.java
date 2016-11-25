package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.IPlugin;

public interface Input extends IPlugin {

    void onInputStart();

    void onInputStop();

    Iterable<Flow> getFlows();
}
