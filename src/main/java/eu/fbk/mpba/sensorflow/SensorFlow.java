package eu.fbk.mpba.sensorflow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SensorFlow {

    //      Fields

    private String sessionTag;
    private volatile boolean closed = false;

    private final Map<String, InputManager> userInputs = new TreeMap<>();
    private final Map<Output, OutputManager> userOutputs = new HashMap<>();

    //      Status Interfaces

    private final InputObserver input = new InputObserver() {
        @Override
        public void inputStatusChanged(InputManager sender, PluginStatus state) {
            // onStatusChanged
        }
    };

    private final OutputObserver output = new OutputObserver() {
        @Override
        public void outputStatusChanged(OutputManager sender, PluginStatus state) {
            // onStatusChanged
        }
    };

    // Data and Events Interface, will be re-added for profiling

    // Engine implementation

    /**
     * Creates an instance of the class and uses a human timestamp to identify the SensorFLow
     * session.
     */
    public SensorFlow() {
        this(new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()));
    }

    /**
     * Creates an instance of the class and allows the customization of the SensorFlow session name.
     *
     * @param sessionTag The custom name of the SensorFlow session.
     */
    public SensorFlow(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    /**
     * Gets the SensorFlow session name.
     *
     * @return A string containing the session name.
     */
    public String getSessionTag() {
        return sessionTag;
    }

    /**
     * How swaps the session name. The session name should not change across a SensorFlow session
     * because the outputs are notified once of its name.
     *
     * @param sessionTag The new name to set.
     */
    protected void setSessionTag(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    //      Plugins

    private SensorFlow add(InputGroup p, boolean routedEverywhere) {
        InputManager added = null;
        // Check if only the name is already contained
        synchronized (userInputs) {
            if (!userInputs.containsKey(p.getName())) {
                added = new InputManager(p, this.input);
                userInputs.put(p.getName(), added);
            }
        }
        if (added != null) {
            // InputGroups are not recursive, just one level
//            p.setManager(this.flow);
            added.onCreate();
            if (routedEverywhere)
                routeAll(added);
            added.onAdded();
        }
        return this;
    }

    /**
     * Adds an input plugin and routes it to every output available at this time.
     *
     * @param p The plugin to add, the only requirement is that it be an InputGroup.
     * @return Returns for a builder usage.
     */
    public SensorFlow add(InputGroup p) {
        return add(p, true);
    }

    /**
     * Adds a collection of input plugins and routes them to every output available at this time.
     *
     * @param p The collection of plugins to add, the only requirement is that it be an InputGroup.
     * @return Returns for a builder usage.
     */
    public SensorFlow add(Collection<InputGroup> p) {
        for (InputGroup inputGroup : p)
            add(inputGroup);
        return this;
    }

    /**
     * Adds an input plugin and routes it to nothing. The data of this input plugin will not be
     * visible until the plugin is routed to an output.
     *
     * @param p The plugin to add, the only requirement is that it be an InputGroup.
     * @return Returns for a builder usage.
     */
    public SensorFlow addNotRouted(InputGroup p) {
        return add(p, false);
    }

    /**
     * Adds a collection of input plugins and routes them to nothing. The data of each input
     * plugin will not be visible until each plugin is routed to an output.
     *
     * @param p The plugin to add, the only requirement is that it be an InputGroup.
     * @return Returns for a builder usage.
     */
    public SensorFlow addNotRouted(Collection<InputGroup> p) {
        for (InputGroup p1 : p)
            addNotRouted(p1);
        return this;
    }

    private void add(Output p, boolean threaded, boolean routeEverywhere) {
        OutputManager added = null;
        // Check if only the name is already contained
        synchronized (userOutputs) {
            if (!userOutputs.containsKey(p)) {
                userOutputs.put(p, added = new OutputManager(p, this.output, threaded));
            }
        }
        if (added != null) {
            // Reversed the following two statements:
            //      Before: onCreate call precedes all data
            //      Now:    some data after onCreate call may be lost.
            //  It is ok as the important rule is that onAdded call precedes all data.
            added.onCreateAndAdded(sessionTag);
            if (routeEverywhere)
                routeAll(added);
        }
    }

    /**
     * Adds an output plugin and routes to it to every input available at this time.
     *
     * @param p The plugin to add, the only requirement is that it be an Output.
     * @return Returns for a builder usage.
     */
    public SensorFlow add(Output p) {
        add(p, true, true);
        return this;
    }

    /**
     * Adds an output plugin and routes nothing to it. This plugin will not receive any data until
     * an input is routed to it.
     *
     * @param p The plugin to add, the only requirement is that it be an Output.
     * @return Returns for a builder usage.
     */
    public SensorFlow addNotRouted(Output p) {
        add(p, true, false);
        return this;
    }

    /**
     * Adds an output plugin and routes to it to every input available at this time. Moreover, the
     * plugin is set to NOT use a buffer, thus the data may come from different threads.
     *
     * @param p The plugin to add, the only requirement is that it be an Output.
     * @return Returns for a builder usage.
     */
    public SensorFlow addInThread(Output p) {
        add(p, false, true);
        return this;
    }

    /**
     * Adds an output plugin and routes nothing to it. This plugin will not receive any data until
     * an input is routed to it.  Moreover, each plugin is set to NOT use a buffer, thus the data
     * may come from different threads.
     *
     * @param p The plugin to add, the only requirement is that it be an Output.
     * @return Returns for a builder usage.
     */
    public SensorFlow addInThreadNotRouted(Output p) {
        add(p, false, false);
        return this;
    }

    /**
     * Removes an input plugin, searching it by its name.
     *
     * @param p The plugin to be removed (comparison only by name).
     * @return Returns for a builder usage.
     */
    public SensorFlow remove(InputGroup p) {
        InputManager removed = null;
        // Check if only the name is already contained
        synchronized (userInputs) {
            if (userInputs.containsKey(p.getName())) {
                removed = userInputs.remove(p.getName());
            }
        }
        if (removed != null) {
            // InputGroups are not recursive, just one level
            for (Input s : p.getChildren()) {
                ArrayList<OutputManager> outputs = new ArrayList<>(s.getOutputs());
                for (OutputManager o : outputs)
                    removeRoute(s, o);
            }
            removed.onRemovedAndClose();
//            p.setManager(null);
        }
        return this;
    }

    /**
     * Removes an output plugin, searching it by its name.
     *
     * @param p The plugin to be removed (comparison only by name).
     * @return Returns for a builder usage.
     */
    public SensorFlow remove(Output p) {
        OutputManager removed = null;
        // Check if only the name is already contained
        synchronized (userOutputs) {
            if (userOutputs.containsKey(p))
                removed = userOutputs.remove(p);
        }
        if (removed != null) {
            ArrayList<Input> inputs = new ArrayList<>(removed.getInputs());
            for (Input i : inputs)
                removeRoute(i, removed);
            // Firstly remove all routes to stop the buffer to be fed
            // then stop and close to empty the buffer
            removed.onStopAndClose();
        }
        return this;
    }

    /**
     * Routes the specified plugins data in a from->to manner. From this call on, the output plugin
     * 'to' will receive the data from the input plugin 'from'.
     *
     * @param from Data source for the link.
     * @param to   Data drain of the link.
     * @return Returns for a builder usage.
     */
    public SensorFlow addRoute(Input from, Output to) {
        if (from != null && to != null) {
            OutputManager outMan;
            synchronized (userOutputs) {
                outMan = userOutputs.get(to);
            }
            addRoute(from, outMan);
        }
        return this;
    }

    /**
     * Removes the route 'from->to'. From this call on, the output plugin 'to' will not receive any
     * more the data from the input plugin 'from'.
     *
     * @param from Data source for the link.
     * @param to   Data drain of the link.
     * @return Returns for a builder usage.
     */
    public SensorFlow removeRoute(Input from, Output to) {
        if (from != null && to != null) {
            OutputManager outMan;
            synchronized (userOutputs) {
                outMan = userOutputs.get(to);
            }
            removeRoute(from, outMan);
        }
        return this;
    }

    /**
     * Returns whether the specified route 'from->to' exists.
     *
     * @param from Data source for the link.
     * @param to   Data drain of the link.
     * @return A boolean value indicating the route existence.
     */
    public boolean isRouted(Input from, Output to) {
        if (from != null && to != null) {
            OutputManager outMan;
            synchronized (userOutputs) {
                outMan = userOutputs.get(to);
            }
            return isRouted(from, outMan);
        }
        return false;
    }

    private void addRoute(Input from, OutputManager outMan) {
        outMan.addInput(from);
        from.addOutput(outMan);
    }

    private void removeRoute(Input fromSensor, OutputManager outMan) {
        fromSensor.removeOutput(outMan);
        outMan.removeInput(fromSensor);
    }

    // Concurrently unsafe
    private boolean isRouted(Input fromSensor, OutputManager outMan) {
        return fromSensor.getOutputs().contains(outMan) && outMan.getInputs().contains(fromSensor);
    }

    /**
     * Enables an output to receive data from its routed inputs. NOTE that an output is always
     * initially disabled.
     *
     * @param o The output to be enabled
     * @return Returns for a builder usage.
     */
    public SensorFlow enableOutput(Output o) {
        return setOutputEnabled(true, o);
    }

    /**
     * Disables an output to receive data from its routed inputs. NOTE that an output is always
     * initially disabled.
     *
     * @param o The output to be disabled
     * @return Returns for a builder usage.
     */
    public SensorFlow disableOutput(Output o) {
        return setOutputEnabled(false, o);
    }

    private SensorFlow setOutputEnabled(boolean enabled, Output o) {
        synchronized (userOutputs) {
            if (userOutputs.containsKey(o)) {
                (userOutputs.get(o)).setEnabled(enabled);
            } else
                throw new IllegalArgumentException("Output not added.");
        }
        return this;
    }

    //      Gets

    /**
     * Returns whether the specified output is enabled.
     *
     * @param o The output to be checked.
     * @return A boolean value indicating if the output is enabled.
     */
    public boolean isOutputEnabled(Output o) {
        boolean b;
        synchronized (userOutputs) {
            b = userOutputs.containsKey(o) && (userOutputs.get(o)).isEnabled();
        }
        return b;
    }

    /**
     * Gets an input plugin by name. Remember to cast it.
     *
     * @param name The name of the plugin to retrieve.
     * @return The plugin instance as an InputGroup
     */
    public InputGroup getInput(String name) {
        InputManager r;
        synchronized (userInputs) {
            r = userInputs.get(name);
        }
        return r == null ? null : r.getInputGroup();
    }

//    public Output getOutput(String name) {
//        Object r;
//        synchronized (userInputs) {
//            r = userOutputs.get(name);
//        }
//        return r == null ? null : ((OutputManager)r).getOutput();
//    }

    /**
     * Gets a collection of all the input plugins.
     *
     * @return A collection containing all the input (and processing) plugins.
     */
    public Collection<InputGroup> getInputs() {
        ArrayList<InputGroup> x;
        synchronized (userInputs) {
            x = new ArrayList<>(userInputs.size());
            for (InputManager o : userInputs.values())
                x.add(o.getInputGroup());
        }
        return Collections.unmodifiableCollection(x);
    }

    /**
     * Gets a collection of all the output plugins.
     *
     * @return A collection containing all the output (and processing) plugins.
     */
    public Collection<Output> getOutputs() {
        ArrayList<Output> x;
        synchronized (userOutputs) {
            x = new ArrayList<>(userOutputs.size());
            for (OutputManager o : userOutputs.values())
                x.add(o.getOutput());
        }
        return Collections.unmodifiableCollection(x);
    }

    //      Engine operation

    /**
     * Removes all the routes. After this call the data will stop flowing. In no case the inputs
     * are blocked.
     *
     * @return Returns for a builder usage.
     */
    public SensorFlow routeClear() {
        // REMOVE ALL
        for (InputManager d : userInputs.values())
            for (Input s : d.getInputs())      // FOREACH SENSOR
                for (OutputManager o : userOutputs.values())    // Remove LINK TO EACH OUTPUT
                    removeRoute(s, o);
        return this;
    }

    /**
     * Routes every input to every output. Plugins that are both (Processing) will not be linked to
     * other Processing plugins. This avoids loops and creates a three layer structure where
     * Inputs send data to every output and to every processing, and where Outputs receive
     * data from every Input and every Processing, but where no Processing receives data from any
     * Processing.
     * If needed, it should be done manually.
     *
     * @return Returns for a builder usage.
     */
    public SensorFlow routeAll() {
        // SENSORS x OUTPUTS
        for (InputManager d : userInputs.values())
            routeAll(d);
        return this;
    }

    private SensorFlow routeAll(InputManager d) {
        for (Input s : d.getInputs())      // FOREACH SENSOR
            // LINK TO EACH OUTPUT
            for (OutputManager o : userOutputs.values())
                if (!(s instanceof Output) || !(o.getOutput() instanceof InputGroup)) {
                    addRoute(s, o);
                } // else is a Processing to Processing: skip
        return this;
    }

    private SensorFlow routeAll(OutputManager o) {
        // SENSORS x OUTPUTS
        for (InputManager d : userInputs.values())
            for (Input s : d.getInputs())      // FOREACH SENSOR
                addRoute(s, o);
        return this;
    }

//    // Does not create duplicates
//    public SensorFlow routeNthToNth() {
//        // max SENSORS, OUTPUTS
//        int maxi = Math.max(userInputs.size(), userOutputs.size());
//        for (int i = 0; i < maxi; i++)                                                                      // FOREACH OF THE LONGEST
//            for (Input s : new ArrayList<>(userInputs.values()).get(i % userInputs.size()).getInputs())      // LINK MODULE LOOPING ON THE SHORTEST
//                addRoute(s, new ArrayList<>(userOutputs.values()).get(i % userOutputs.size()));
//        return this;
//    }

//    private void changeStatus(Status status) {
//        this.closed = status;
//    }

//    public Status getStatus() {
//        return closed;
//    }

    /**
     * Stops everything and frees the resources.
     */
    public synchronized void close() {
        if (!closed) {
            for (InputManager d : userInputs.values()) {
//                    for (Input s : d.getInputGroup().getChildren()) {
//                        s.onRemoved();
//                        s.onClose();
//                    }
                d.getInputGroup().onRemoved();
                d.getInputGroup().onClose();
            }
            routeClear();
            for (OutputManager o : userOutputs.values())
                o.onStopAndClose();
            closed = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
