package eu.fbk.mpba.sensorflow;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation requires to SensorFlow that the annotated output plugin be run with an input
 * buffer. This causes the data to come from a single thread and the inputs to have some time
 * before being blocked by a delay in returning from the callbacks in the output.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleThreadRequired {
}
