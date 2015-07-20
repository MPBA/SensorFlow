package eu.fbk.mpba.sensorsflows.base;

/**
 * Exposes methods to know how many samples have been received from the object and how many of them
 * have been forwarded since the call's time.
 */
public interface ISampleCounter {
    int getReceivedMessagesCount();
    int getForwardedMessagesCount();
}
