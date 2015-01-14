package eu.fbk.mpba.sensorsflows;

public enum AutoLinkMode {
    /**
     * Uses the links specified through the {@code addLink} method.
     */
    MANUAL,
    /**
     * Links each sensor to each output.
     */
    PRODUCT,
    /**
     * Links each nth element of the longest collection between sensors and outputs to the nth
     * element of the other. When the length is not the same the modulo operation is used.
     */
    NTH_TO_NTH
}
