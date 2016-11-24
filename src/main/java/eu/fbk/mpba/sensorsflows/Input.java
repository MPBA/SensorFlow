package eu.fbk.mpba.sensorsflows;

import eu.fbk.mpba.sensorsflows.base.IPlugin;

public interface Input<TimeT, ValueT> extends IPlugin {

    void onInputStart();

    void onInputStop();

    Iterable<Flow<TimeT, ValueT>> getFlows();
}
