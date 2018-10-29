package eu.fbk.mpba.sensorflow.sense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;

/**
 * Base class for an InputModule
 */
public abstract class InputModule extends Module {
    private final ArrayList<Stream> children = new ArrayList<>();
    private boolean added = false;

    /**
     * Constructor of abstract class
     */
    protected InputModule() { }

    /**
     * Adds a Stream to the InputModule. The Input must have an unique name within the device inputs.
     * An Input can be added to the WirelessDevice scheme in any moment.
     * Special Inputs may be already present.
     *
     * @param input The flow to add to the InputModule.
     */
    protected void addStream(Stream input) {
        if (!added) {
            children.add(input);
        } else {
            throw new UnsupportedOperationException("Online streams changes not supported. Add streams before onAdded or after onRemoved.");
        }
    }

    protected Collection<Stream> getStreams() {
        return children;
    }

    @Override
    public final Collection<Input> getChildren() {
        ArrayList<Input> sfPlugins = new ArrayList<>(super.getChildren());
        sfPlugins.addAll(children);
        return Collections.unmodifiableCollection(sfPlugins);
    }

    public abstract void onCreate();

    public void onAdded() {
        added = true;
    }

    public void onRemoved() {
        added = false;
    }

    public abstract void onClose();
}
