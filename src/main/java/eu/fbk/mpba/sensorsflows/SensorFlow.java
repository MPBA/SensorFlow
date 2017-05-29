package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * SensorFlow is the class that represents the engine of the library.
 * This is the only interface that the user should use.
 * Implementation of the IManager
 */
public class SensorFlow implements
        InputObserver,
        FlowObserver,
        OutputObserver {

//    public static Face face;

//    public interface Face {
//        void println(String m);
//    }

    interface Callback<T, A> {
        void handle(T sender, A state);
    }

    // Status Interface

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void inputStatusChanged(InputManager sender, Input.Status state) {
        if (sender != null) {
            if (state == Input.Status.INITIALIZED) {
                synchronized (_itemsToInitLock) {
                    if (_devicesToInit.contains(sender)) {
                        _devicesToInit.remove(sender);
                        if (_status == SensorFlow.Status.PREPARING && _devicesToInit.isEmpty()) {
                            // POI Change point
                            _devicesToInit = null;
                        }
                    }
                }
                if (_outputsToInit == null)
                    // FIXME WARN User-code time dependency in the output thread or child
                    changeStatus(SensorFlow.Status.STREAMING);
            }
            if (_onDeviceStatusChanged != null)
                _onDeviceStatusChanged.handle(sender.getInput(), state);
        }
    }

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void outputStatusChanged(OutputManager sender, Output.Status state) {
        if (sender != null) {
            if (state == Output.Status.INITIALIZED) {
                synchronized (_itemsToInitLock) {
                    if (_outputsToInit.contains(sender)) {
                        _outputsToInit.remove(sender);
                        if (_status == SensorFlow.Status.PREPARING && _outputsToInit.isEmpty()) {
                            // POI Change point
                            _outputsToInit = null;
                        }
                    }
                }
                if (_devicesToInit == null)
                    // FIXME WARN User-code time dependency in the output thread or son
                    changeStatus(SensorFlow.Status.STREAMING);
            }
            if (_onOutputStatusChanged != null)
                _onOutputStatusChanged.handle(sender.getOutput(), state);
        }
    }

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void onStatusChanged(Flow sender, long time, Flow.Status state) {
        // TODO 3 Implement an 'internal input' with an 'internal flow' for log utilities.
        // The flow has to send also an event on a status change.
    }

    // Data and Events Interface

    /**
     * Not for the end-user.
     * The flow calls this when it has a new value.
     *
     * @param sender sender
     * @param time   timestamp
     * @param value  value
     */
    @Override
    public void onValue(Flow sender, long time, double[] value) {
        if (!sender.isMuted() && !_paused) {
            for (OutputManager o : sender.getOutputs()) {
                if (o.isEnabled())
                    //noinspection unchecked
                    o.onValue(sender, time, value);
            }
        }
    }

    /**
     * Not for the end-user.
     *
     * @param sender  sender
     * @param type    event code
     * @param message message text
     */
    @Override
    public void onEvent(Flow sender, long time, int type, String message) {
        if (!sender.isMuted() && !_paused) {
            for (OutputManager o : sender.getOutputs()) {
                if (o.isEnabled())
                    //noinspection unchecked
                    o.onEvent(sender, time, type, message);
            }
        }
    }

    //      no deviceEvent
    //      no outputEvent for now

    // Fields

    private final String _emAlreadyRendered = "The engine is initialized. No inputs, outputs or links can be added now.";
    private final String _itemsToInitLock = "_itemsToInitLock";

    private String sessionTag = "";
    private Status _status = SensorFlow.Status.STANDBY;
    private boolean _paused = false;

    private Map<String, InputManager> _userDevices = new TreeMap<>();
    private Map<String, OutputManager> _userOutputs = new TreeMap<>();

    private List<InputManager> _devicesToInit = new ArrayList<>();                         // null
    private List<OutputManager> _outputsToInit = new ArrayList<>();                        // null

    private Callback<SensorFlow, Status> _onStatusChanged = null;                 // null
    private Callback<Input, Input.Status> _onDeviceStatusChanged = null;               // null
    private Callback<Output, Output.Status> _onOutputStatusChanged = null;             // null

    // Engine implementation

    /**
     * Default constructor.
     */
    public SensorFlow() {
        changeStatus(SensorFlow.Status.STANDBY);
    }

    public String getSessionTag() {
        return sessionTag;
    }

    public void setSessionTag(String sessionTag) {
        if (_status == SensorFlow.Status.STANDBY) {
            this.sessionTag = sessionTag;
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    //      STANDBY inputs (proper)

    /**
     * Adds a device to the enumeration, this is to be used before the {@code start} call, before the internal IO-mapping.
     *
     * @param input Device to add.
     */
    public SensorFlow addInput(Input input) {
        if (_status == SensorFlow.Status.STANDBY) {
            // Check if only the name is already contained
            if (!_userDevices.containsKey(input.getName())) {
                _userDevices.put(input.getName(), new InputManager(input, this));
                for (Flow s : input.getFlows())
                    s.addHandler(this);
            }
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
        return this;
    }

    public Input getInput(String name) {
        InputManager r = _userDevices.get(name);
        //noinspection unchecked
        return r == null ? null : r.getInput();
    }

    /**
     * Adds a link between a flow and an output (N to M relation) before the {@code start} call.
     *
     * @param fromSensor Input flow retreived from a device.
     * @param toOutput   Output channel.
     */
    public SensorFlow addLink(Flow fromSensor, Output toOutput) {
        if (fromSensor != null && toOutput != null)
            // Manual indexOf for performance
            for (OutputManager outMan : _userOutputs.values())
                if (toOutput == outMan.getOutput()) { // for reference, safe
                    addLink(fromSensor, outMan);
                    break;
                }
        return this;
    }

    /**
     * Removes a link between a flow and an output (N to M relation) before the {@code start} call.
     * TODO: test
     * @param fromSensor Input flow retrieved from a device.
     * @param toOutput   Output channel.
     */
    public SensorFlow removeLink(Flow fromSensor, Output toOutput) {
        // Manual indexOf for performance
        for (OutputManager outMan : _userOutputs.values())
            if (toOutput == outMan.getOutput()) { // for reference, safe
                removeLink(fromSensor, outMan);
                break;
            }
        return this;
    }

    public SensorFlow setOutputEnabled(boolean enabled, String name) {
        if (_userOutputs.containsKey(name)) {
            (_userOutputs.get(name)).setEnabled(enabled);
        }
        return this;
    }

    public boolean getOutputEnabled(String name) {
        return _userOutputs.containsKey(name) && (_userOutputs.get(name)).isEnabled();
    }

    /**
     * Adds a link between a flow and an output-decorator object (N to M relation) before the {@code start} call.
     *
     * @param fromSensor Input flow retrieved from a device.
     * @param outMan     OutputManager object.
     */
    private void addLink(Flow fromSensor, OutputManager outMan) {
        if (_status == SensorFlow.Status.STANDBY) {
            fromSensor.addOutput(outMan);
            outMan.addFlow(fromSensor);
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    /**
     * Removes a link between a flow and an output-decorator object (N to M relation) before the {@code start} call.
     * TODO: test
     * @param fromSensor Input flow retrieved from a device.
     * @param outMan     OutputManager object.
     */
    private void removeLink(Flow fromSensor, OutputManager outMan) {
        if (_status == SensorFlow.Status.STANDBY) {
            fromSensor.removeOutput(outMan);
            outMan.removeFlow(fromSensor);
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    /**
     * Adds an output to the enumeration, this is to be used before the {@code start} call, before the internal in-out map rendering.
     *
     * @param output Output to add.
     */
    public SensorFlow addOutput(Output output) {
        if (_status == SensorFlow.Status.STANDBY) {
            // Check if only the name is already contained
            if (!_userOutputs.containsKey(output.getName()))
                _userOutputs.put(output.getName(), new OutputManager(output, this));
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
        return this;
    }

    public Output getOutput(String name) {
        Object r = _userOutputs.get(name);
        //noinspection unchecked
        return r == null ? null : ((OutputManager)r).getOutput();
    }

    //      STANDBY aux gets (proper)

    /**
     * Enumerates every Input managed.
     *
     * @return Enumerator usable trough a for (IInputManager d : enumerator)
     */
    public Iterable<Input> getInputs() {
        return new Iterable<Input>() {
            @Override
            public Iterator<Input> iterator() {
                final Iterator<InputManager> i = _userDevices.values().iterator();
                return new Iterator<Input>() {

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public Input next() {
                        return i.next().getInput();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Cannot remove objects from here.");
                    }
                };
            }
        };
    }

    /**
     * Enumerates every Output managed.
     *
     * @return Enumerator usable trough a for (IOutput o : enumerator)
     */
    public Iterable<Output> getOutputs() {
        return new Iterable<Output>() {
            @Override
            public Iterator<Output> iterator() {
                final Iterator<OutputManager> i = _userOutputs.values().iterator();
                return new Iterator<Output>() {

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public Output next() {
                        return i.next().getOutput();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Cannot remove objects from here.");
                    }
                };
            }
        };
    }

    //      Internal init and final management

    /**
     * This method allows to initialize the device before the {@code start} call.
     * Made private
     *
     * @param device {@code IInputManager} to initializeInput
     */
    private void initialize(InputManager device) {
        // The connection state is checked before the start end callback.
        //noinspection StatementWithEmptyBody
        if (/*_decDevices.contains(device) &&  */device.getStatus() == Input.Status.NOT_INITIALIZED) {
            device.initializeInput();
        } else {
//            Log.w(LOG_TAG, "IInputManager not NOT_INITIALIZED: " + device.toString());
        }
    }

    /**
     * This method allows to initializeInput the device before the {@code start} call.
     * Made private
     *
     * @param output {@code IOutput} to finalize.
     */
    private void initialize(OutputManager output, String sessionName) {
        //noinspection StatementWithEmptyBody
        if (/*_decOutputs.contains(output) &&  */output.getStatus() == Output.Status.NOT_INITIALIZED) {
            output.initializeOutput(sessionName);
        } else {
//            Log.w(LOG_TAG, "IOutput not NOT_INITIALIZED: " + output.toString());
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Made private
     *
     * @param device {@code IInputManager} to finalize.
     */
    private void finalize(InputManager device) {
        // The connection state is not checked
        //noinspection StatementWithEmptyBody
        if (/*_decDevices.contains(device) &&  */device.getStatus() == Input.Status.INITIALIZED) {
            device.finalizeInput();
        } else {
//            Log.w(LOG_TAG, "IInputManager not INITIALIZED: " + device.toString());
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Made private
     *
     * @param output {@code IOutput} to finalize.
     */
    private void finalize(OutputManager output) {
        //noinspection StatementWithEmptyBody
        if (/*_decOutputs.contains(output) &&  */output.getStatus() == Output.Status.INITIALIZED) {
            output.finalizeOutput();
        } else {
//            Log.w(LOG_TAG, "IOutput not INITIALIZED: " + output.toString());
        }
    }

    //      Engine operation

    public SensorFlow routeAll() {
        // SENSORS x OUTPUTS
        for (InputManager d : _userDevices.values())
            for (Flow s : d.getFlows())      // FOREACH SENSOR
                for (OutputManager o : _userOutputs.values())    // LINK TO EACH OUTPUT
                    addLink(s, o);
        return this;
    }

    public SensorFlow routeNthToNth() {
        // max SENSORS, OUTPUTS
        int maxi = Math.max(_userDevices.size(), _userOutputs.size());
        for (int i = 0; i < maxi; i++)                                                                      // FOREACH OF THE LONGEST
            for (Flow s : new ArrayList<>(_userDevices.values()).get(i % _userDevices.size()).getFlows())      // LINK MODULE LOOPING ON THE SHORTEST
                addLink(s, new ArrayList<>(_userOutputs.values()).get(i % _userOutputs.size()));
        return this;
    }

    /**
     * Renders the IO-mapping and in two times (async.) initializes the devices and the outputs.
     * <p/>
     * If a device/output was initialized before this call and it is not already INITIALIZED the
     * engine will wait for it for an indefinite timestamp. In this period the engine status will stay
     * {@code Status.PREPARING}.
     * <p/>
     * The session name is the date-timestamp string {@code Long.toString(System.currentTimeMillis())}
     * if the sessionTag has not been set.
     */
    public SensorFlow start() {
        if (sessionTag == null || sessionTag.length() == 0)
            sessionTag = Long.toString(System.currentTimeMillis());
        return start(sessionTag);
    }

    /**
     * Renders the IO-mapping and in two times (async.) initializes the devices and the outputs.
     * <br/>
     * If a device/output was initialized before this call and it is not already INITIALIZED the
     * engine will wait for it for an indefinite time. In this period the engine status will stay
     * {@code Status.PREPARING}.
     * <p/>
     * Allows to give a name to the current session but it DOES NOT CHECK if it already exists.
     */
    private SensorFlow start(String sessionName) {
        if (getStatus() == SensorFlow.Status.STANDBY
                || getStatus() == SensorFlow.Status.CLOSED) {
            changeStatus(SensorFlow.Status.PREPARING);
            _devicesToInit.addAll(_userDevices.values());
            // Launches the initializations
            for (InputManager d : _userDevices.values()) {
                // only if NOT_INITIALIZED: checked in the initializeInput method
                initialize(d);
            }
            _outputsToInit.addAll(_userOutputs.values());
            for (OutputManager o : _userOutputs.values()) {
                // only if NOT_INITIALIZED: checked in the initializeInput method
                initialize(o, sessionName);
            }
            // WAS _outputsSensors.clear();
            // WAS _outputsSensors = null;
        } else
            throw new UnsupportedOperationException("Engine already running!");
        return this;
    }

    /**
     * Returns weather the global streaming is paused.
     *
     * @return Boolean value.
     */
    public boolean isPaused() {
        return _paused;
    }

    /**
     * Allows to pause or to resume the streaming in the faster way.
     *
     * @param paused Boolean value.
     */
    public SensorFlow setPaused(boolean paused) {
        _paused = paused;
        return this;
    }

    private void changeStatus(Status status) {
        _status = status;
        if (_onStatusChanged != null)
            _onStatusChanged.handle(this, _status);
    }

    /**
     * Gets the status of the engine.
     *
     * @return The actual status of the engine.
     */
    public Status getStatus() {
        return _status;
    }

    /**
     * This method finalizes every device and every output and waits the queues to get empty.
     */
    public SensorFlow stop() {
        changeStatus(SensorFlow.Status.FINALIZING);
        for (InputManager d : _userDevices.values()) {
            // only if INITIALIZED: checked in the method
            finalize(d);
        }
        for (OutputManager o : _userOutputs.values()) {
            // only if INITIALIZED: checked in the method
            finalize(o);
        }
        changeStatus(SensorFlow.Status.FINALIZED);
        return this;
    }

    /**
     *
     */
    public void close() {
        switch (getStatus()) {
            case STANDBY:
                break;
            case STREAMING:
                stop();
            case FINALIZED:
                changeStatus(SensorFlow.Status.CLOSING);
                for (InputManager d : _userDevices.values())
                    for (Flow s : d.getInput().getFlows())
                        s.close();
                for (OutputManager o : _userOutputs.values())
                    o.close();
                changeStatus(SensorFlow.Status.CLOSED);
                break;
            case CLOSED:
                break;
            default:
                throw new UnsupportedOperationException(
                        "Another operation is trying to change the state: " +
                                getStatus().toString());
        }
    }

    /**
     * Finalizes the object calling also the {@code close} method.
     *
     * @throws Throwable any error
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        // After, Object.finalize()
        super.finalize();
    }

    /**
     * Sets a listener to receive the engine state changes.
     *
     * @param callback Callback to call when the engine state changes.
     */
    public SensorFlow setOnStatusChanged(Callback<SensorFlow, Status> callback) {
        _onStatusChanged = callback;
        return this;
    }

    /**
     * Sets a listener to receive every device's state change.
     *
     * @param callback Callback to call when any device's state changes.
     */
    public SensorFlow setOnInputStatusChanged(Callback<Input, Input.Status> callback) {
        _onDeviceStatusChanged = callback;
        return this;
    }

    /**
     * Sets a listener to receive every output's state change.
     *
     * @param callback Callback to call when any device's state changes.
     */
    public SensorFlow setOnOutputStatusChanged(Callback<Output, Output.Status> callback) {
        _onOutputStatusChanged = callback;
        return this;
    }

    public enum Status {
        STANDBY, PREPARING, STREAMING, FINALIZING, FINALIZED, CLOSING, CLOSED
    }
}
