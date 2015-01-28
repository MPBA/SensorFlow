package eu.fbk.mpba.sensorsflowsa.base;

public interface EventCallback<T, A> {
    void handle(T sender, A state);
}
