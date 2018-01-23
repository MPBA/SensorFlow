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

    private final String sessionTag;
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

    public SensorFlow() {
        this(new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date()));
    }

    public SensorFlow(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    public String getSessionTag() {
        return sessionTag;
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

    public SensorFlow add(InputGroup p) {
        return add(p, true);
    }

    public SensorFlow add(Collection<InputGroup> p) {
        for (InputGroup inputGroup : p)
            add(inputGroup);
        return this;
    }

    public SensorFlow addNotRouted(InputGroup p) {
        return add(p, false);
    }

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

    public SensorFlow add(Output p) {
        add(p, true, true);
        return this;
    }

    public SensorFlow addNotRouted(Output p) {
        add(p, true, false);
        return this;
    }

    public SensorFlow addInThread(Output p) {
        add(p, false, true);
        return this;
    }

    public SensorFlow addInThreadNotRouted(Output p) {
        add(p, false, false);
        return this;
    }

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

    public SensorFlow remove(Output p) {
        OutputManager removed = null;
        // Check if only the name is already contained
        synchronized (userOutputs) {
            if (userOutputs.containsKey(p))
                removed = userOutputs.remove(p);
        }
        if (removed != null) {
            final OutputManager o = removed;
            ArrayList<Input> inputs = new ArrayList<>(o.getInputs());
            for (Input i : inputs)
                removeRoute(i, o);
            // Firstly remove all routes to stop the buffer to be fed
            // then stop and close to empty the buffer
            o.onStopAndClose();
        }
        return this;
    }

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

    public SensorFlow enableOutput(Output o) {
        return setOutputEnabled(true, o);
    }

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

    public boolean isOutputEnabled(Output o) {
        boolean b;
        synchronized (userOutputs) {
            b = userOutputs.containsKey(o) && (userOutputs.get(o)).isEnabled();
        }
        return b;
    }

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

    public Collection<InputGroup> getInputs() {
        ArrayList<InputGroup> x;
        synchronized (userInputs) {
            x = new ArrayList<>(userInputs.size());
            for (InputManager o : userInputs.values())
                x.add(o.getInputGroup());
        }
        return Collections.unmodifiableCollection(x);
    }

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

    // Does not create duplicates
    public SensorFlow routeClear() {
        // REMOVE ALL
        for (InputManager d : userInputs.values())
            for (Input s : d.getInputs())      // FOREACH SENSOR
                for (OutputManager o : userOutputs.values())    // Remove LINK TO EACH OUTPUT
                    removeRoute(s, o);
        return this;
    }

    // Does not create duplicates
    public SensorFlow routeAll() {
        // SENSORS x OUTPUTS
        for (InputManager d : userInputs.values())
            routeAll(d);
        return this;
    }

    private SensorFlow routeAll(InputManager d) {
        for (Input s : d.getInputs())      // FOREACH SENSOR
            for (OutputManager o : userOutputs.values())    // LINK TO EACH OUTPUT
                addRoute(s, o);
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
