package eu.fbk.mpba.sensorflow.sense;

import java.util.ArrayList;
import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;

/**
 * Base class for an InputModule
 */
public abstract class InputModule extends Module implements InputGroup {
    private final ArrayList<Input> children = new ArrayList<>();

    /**
     * Constructor of abstract class
     * @param name Name of the Module.
     * @param settings Configuration string (e.g. json) to be passed to the Module.
     */
    public InputModule(String name, String settings) {
        super(name, settings);
        addSFChild(this);
    }

    /**
     * Adds a Stream to the InputModule. The Input must have an unique name within the device inputs.
     * An Input can be added to the WirelessDevice scheme in any moment.
     * Special Inputs may be already present.
     *
     * @param input The flow to add to the InputModule.
     */
    protected void addStream(Stream input) {
        children.add(input);
    }

    @Override
    public Iterable<Input> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public abstract void onCreate();

    public abstract void onAdded();

    public abstract void onRemoved();

    public abstract void onClose();
}
