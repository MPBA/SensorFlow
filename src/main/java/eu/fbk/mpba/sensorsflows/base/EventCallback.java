package eu.fbk.mpba.sensorsflows.base;

public interface EventCallback<T, A> {
    void handle(T sender, A state);
}
