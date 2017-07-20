package eu.fbk.mpba.sensorflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class SensorFlow {

    // Fields

    private final String sessionTag;
    private Status _status = SensorFlow.Status.READY;
    private boolean muted = false;

    private final Map<String, InputManager> _userInputs = new TreeMap<>();
    private final Map<String, OutputManager> _userOutputs = new TreeMap<>();

    // Status Interfaces

    private final InputObserver input = (sender, state) -> {
        // onStatusChanged
    };

    private final OutputObserver output = (sender, state) -> {
        // onStatusChanged
    };

    // Data and Events Interface

    private final DataObserver flow = new DataObserver() {

        @Override
        public void onValue(Input sender, long time, double[] value) {
            if (!sender.isMuted() && !muted) {
                for (OutputManager o : sender.getOutputs()) {
                    if (o.isEnabled())
                        //noinspection unchecked
                        o.pushValue(sender, time, value);
                }
            }
        }

        @Override
        public void onLog(Input sender, long time, String message) {
            if (!sender.isMuted() && !muted) {
                for (OutputManager o : sender.getOutputs()) {
                    if (o.isEnabled())
                        //noinspection unchecked
                        o.pushLog(sender, time, message);
                }
            }
        }
    };

    // Engine implementation

    public SensorFlow() {
        this(Long.toString(System.currentTimeMillis()));
    }

    public SensorFlow(String sessionTag) {
        this.sessionTag = sessionTag;
        changeStatus(Status.READY);
    }

    public String getSessionTag() {
        return sessionTag;
    }

    //      Plugins

    public SensorFlow add(Input p) {
            InputManager added = null;
            // Check if only the name is already contained
            synchronized (_userInputs) {
                if (!_userInputs.containsKey(p.getName())) {
                    added = new InputManager(p, this.input);
                    _userInputs.put(p.getName(), added);
                }
            }
            if (added != null) {
                // InputGroups are not recursive, just one level
                p.setManager(this.flow);
                added.onCreate();
            }
        return this;
    }

    public SensorFlow add(Output p) {
        return add(p, true);
    }

    public SensorFlow add(Output p, boolean threaded) {
        OutputManager added = null;
        // Check if only the name is already contained
        synchronized (_userOutputs) {
            if (!_userOutputs.containsKey(p.getName())) {
                _userOutputs.put(p.getName(), added = new OutputManager(p, this.output));
            }
        }
        if (added != null) {
            added.onCreate(sessionTag);
        }
        return this;
    }

    public SensorFlow remove(Input p) {
        InputManager removed = null;
        // Check if only the name is already contained
        synchronized (_userInputs) {
            if (_userInputs.containsKey(p.getName())) {
                removed = _userInputs.remove(p.getName());
            }
        }
        if (removed != null) {
            // InputGroups are not recursive, just one level
            for (Input s : p.getChildren()) {
                ArrayList<OutputManager> outputs = new ArrayList<>(s.getOutputs());
                outputs.forEach((o) -> removeRoute(s, o));
            }
            removed.onClose();
            p.setManager(null);
        }
        return this;
    }

    public SensorFlow remove(Output p) {
        OutputManager removed = null;
        // Check if only the name is already contained
        synchronized (_userOutputs) {
            if (_userOutputs.containsKey(p.getName()))
                removed = _userOutputs.remove(p.getName());
        }
        if (removed != null) {
            final OutputManager o = removed;
            ArrayList<Input> inputs = new ArrayList<>(o.getInputs());
            inputs.forEach((i) -> removeRoute(i, o));
            o.onClose();
        }
        return this;
    }

    public SensorFlow addRoute(Input from, Output to) {
        if (from != null && to != null) {
            OutputManager outMan;
            synchronized (_userOutputs) {
                outMan = _userOutputs.get(to.getName());
            }
            addRoute(from, outMan);
        }
        return this;
    }

    public SensorFlow removeRoute(Input from, Output to) {
        if (from != null && to != null) {
            OutputManager outMan;
            synchronized (_userOutputs) {
                outMan = _userOutputs.get(to.getName());
            }
            removeRoute(from, outMan);
        }
        return this;
    }

    private void addRoute(Input from, OutputManager outMan) {
        // Put in sets: no duplicates
        from.addOutput(outMan);
        outMan.addInput(from);
    }

    private void removeRoute(Input fromSensor, OutputManager outMan) {
        fromSensor.removeOutput(outMan);
        outMan.removeInput(fromSensor);
    }

    public SensorFlow enableOutput(String name) {
        return setOutputEnabled(true, name);
    }

    public SensorFlow disableOutput(String name) {
        return setOutputEnabled(false, name);
    }

    private SensorFlow setOutputEnabled(boolean enabled, String name) {
        synchronized (_userOutputs) {
            if (_userOutputs.containsKey(name)) {
                (_userOutputs.get(name)).setEnabled(enabled);
            }
        }
        return this;
    }

    //      Gets

    public boolean isStreamingEnabled() {
        return muted;
    }

    public boolean isOutputEnabled(String name) {
        synchronized (_userOutputs) {
            return _userOutputs.containsKey(name) && (_userOutputs.get(name)).isEnabled();
        }
    }

    public InputGroup getInput(String name) {
        InputManager r;
        synchronized (_userInputs) {
            r = _userInputs.get(name);
        }
        return r == null ? null : r.getInput();
    }

    public Output getOutput(String name) {
        Object r;
        synchronized (_userInputs) {
            r = _userOutputs.get(name);
        }
        return r == null ? null : ((OutputManager)r).getOutput();
    }

    public Iterable<InputGroup> getInputs() {
        ArrayList<InputGroup> x;
        synchronized (_userInputs) {
            x = new ArrayList<>(_userInputs.size());
            _userInputs.values().forEach((o) -> x.add(o.getInput()));
        }
        return Collections.unmodifiableCollection(x);
    }

    public Iterable<Output> getOutputs() {
        ArrayList<Output> x;
        synchronized (_userOutputs) {
            x = new ArrayList<>(_userOutputs.size());
            _userOutputs.values().forEach((o) -> x.add(o.getOutput()));
        }
        return Collections.unmodifiableCollection(x);
    }

    //      Engine operation

    public void disableStreaming() {
        setStreamsMuted(true);
    }

    public void enableStreaming() {
        setStreamsMuted(true);
    }

    public SensorFlow setStreamsMuted(boolean muted) {
        this.muted = muted;
        return this;
    }

    // Does not create duplicates
    public SensorFlow routeClear() {
        // REMOVE ALL
        for (InputManager d : _userInputs.values())
            for (Input s : d.getFlows())      // FOREACH SENSOR
                for (OutputManager o : _userOutputs.values())    // Remove LINK TO EACH OUTPUT
                    removeRoute(s, o);
        return this;
    }

    // Does not create duplicates
    public SensorFlow routeAll() {
        // SENSORS x OUTPUTS
        for (InputManager d : _userInputs.values())
            for (Input s : d.getFlows())      // FOREACH SENSOR
                for (OutputManager o : _userOutputs.values())    // LINK TO EACH OUTPUT
                    addRoute(s, o);
        return this;
    }

    // Does not create duplicates
    public SensorFlow routeNthToNth() {
        // max SENSORS, OUTPUTS
        int maxi = Math.max(_userInputs.size(), _userOutputs.size());
        for (int i = 0; i < maxi; i++)                                                                      // FOREACH OF THE LONGEST
            for (Input s : new ArrayList<>(_userInputs.values()).get(i % _userInputs.size()).getFlows())      // LINK MODULE LOOPING ON THE SHORTEST
                addRoute(s, new ArrayList<>(_userOutputs.values()).get(i % _userOutputs.size()));
        return this;
    }

    private void changeStatus(Status status) {
        _status = status;
    }

    public Status getStatus() {
        return _status;
    }

    public void close() {
        switch (getStatus()) {
            case READY:
                changeStatus(Status.CLOSING);
                for (InputManager d : _userInputs.values()) {
                    for (Input s : d.getInput().getChildren())
                        s.onClose();
                    d.getInput().onClose();
                }
                _userOutputs.values().forEach(OutputManager::close);
                changeStatus(Status.CLOSED);
                break;
            case CLOSED:
                break;
            default:
                throw new UnsupportedOperationException(
                        "Another operation is trying to change the state: " +
                                getStatus().toString());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        // After, Object.onClose()
        super.finalize();
    }

    public enum Status {
        READY, CLOSING, CLOSED
    }
}
