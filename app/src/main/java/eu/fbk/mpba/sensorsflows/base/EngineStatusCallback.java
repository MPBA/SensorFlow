package eu.fbk.mpba.sensorsflows.base;

public interface EngineStatusCallback<T> {
    void handle(T sender, EngineStatus state);
}
