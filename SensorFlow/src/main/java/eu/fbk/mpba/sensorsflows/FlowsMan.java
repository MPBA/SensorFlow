package eu.fbk.mpba.sensorsflows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.base.EventCallback;
import eu.fbk.mpba.sensorsflows.base.IDeviceCallback;
import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.IOutputCallback;
import eu.fbk.mpba.sensorsflows.base.ISensorDataCallback;
import eu.fbk.mpba.sensorsflows.base.IUserInterface;
import eu.fbk.mpba.sensorsflows.base.OutputStatus;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * FlowsMan is the class that represents the engine of the library.
 * This is the only interface that the user should use.
 * Implementation of the IUserInterface
 * @param <TimeT> The type of the timestamp returned by the outputs (must be the same for every item).
 * @param <ValueT> The type of the value returned by the devices (must be the same for every item).
 */
public class FlowsMan<TimeT, ValueT> implements
        IUserInterface<NodePlugin<TimeT, ValueT>, SensorComponent<TimeT, ValueT>, OutputPlugin<TimeT, ValueT>>,
        IDeviceCallback<NodeDecorator<TimeT, ValueT>>,
        ISensorDataCallback<SensorComponent<TimeT, ValueT>, TimeT, ValueT>,
        IOutputCallback<TimeT, ValueT> {

    // Status Interface

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void deviceStatusChanged(NodeDecorator sender, DeviceStatus state) {
        if (state == DeviceStatus.INITIALIZED) {
            synchronized (_itemsToInitLock) {
                if (_devicesToInit.contains(sender)) {
                    _devicesToInit.remove(sender);
                    if (_status == EngineStatus.PREPARING && _devicesToInit.isEmpty()) {
                        // POI Change point
                        _devicesToInit = null;
                    }
                }
            }
            if (_outputsToInit == null)
                // FIXME WARN User-code timestamp dependency in the output thread or son
                changeStatus(EngineStatus.STREAMING);
        }
        // TODO 7 Manage the other states
    }

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void outputStatusChanged(IOutput<TimeT, ValueT> sender, OutputStatus state) {
        if (state == OutputStatus.INITIALIZED) {
            synchronized (_itemsToInitLock) {
                if (_outputsToInit.contains(sender)) {
                    _outputsToInit.remove(sender);
                    if (_status == EngineStatus.PREPARING && _outputsToInit.isEmpty()) {
                        // POI Change point
                        _outputsToInit = null;
                    }
                }
            }
            if (_devicesToInit == null)
                // FIXME WARN User-code timestamp dependency in the output thread or son
                changeStatus(EngineStatus.STREAMING);
        }
        // TODO 7 Manage the other states
    }

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void sensorStatusChanged(SensorComponent<TimeT, ValueT> sender, TimeT time, SensorStatus state) {
        // TODO 3 Implement an 'internal device' with an 'internal sensor' for log utilities.
        // The sensor has to send also an event on a status change.
    }

    // Data and Events Interface

    /**
     * Not for the end-user.
     * The sensor calls this when it has a new value.
     *
     * @param sender sender
     * @param time   timestamp
     * @param value  value
     */
    @Override
    public void sensorValue(SensorComponent<TimeT, ValueT> sender, TimeT time, ValueT value) {
        if (sender.isListened() && !_paused) {
            for (OutputDecorator o : sender.getOutputs()) {
                if (o.isEnabled())
                    //noinspection unchecked
                    o.sensorValue(sender, time, value);
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
    public void sensorEvent(SensorComponent<TimeT, ValueT> sender, TimeT time, int type, String message) {
        if (sender.isListened() && !_paused) {
            for (OutputDecorator o : sender.getOutputs()) {
                if (o.isEnabled())
                    //noinspection unchecked
                    o.sensorEvent(sender, time, type, message);
            }
        }
    }

    //      no deviceEvent
    //      no outputEvent for now

    // Fields

    final String _emAlreadyRendered = "The engine is initialized. No inputs, outputs or links can be added now.";
    final String _itemsToInitLock = "_itemsToInitLock";

    private LinkMode _linkMode = LinkMode.PRODUCT;
    private String sessionTag = "";
    protected EngineStatus _status = EngineStatus.STANDBY;
    protected boolean _paused = false;

    // maybe key, value

    protected Map<String, NodeDecorator<TimeT, ValueT>> _userDevices = new TreeMap<>();
    protected Map<String, OutputDecorator<TimeT, ValueT>> _userOutputs = new TreeMap<>();

    protected List<NodeDecorator> _devicesToInit = new ArrayList<>();                                                 // null
    protected List<IOutput> _outputsToInit = new ArrayList<>();                                                         // null

    protected EventCallback<IUserInterface<NodePlugin<TimeT, ValueT>, SensorComponent<TimeT, ValueT>, OutputPlugin<TimeT, ValueT>>
            , EngineStatus> _onStatusChanged = null;                                                                     // null
    protected EventCallback<NodePlugin<TimeT, ValueT>, DeviceStatus> _onDeviceStatusChanged = null;                    // null
    protected EventCallback<OutputPlugin<TimeT, ValueT>, OutputStatus> _onOutputStatusChanged = null;                    // null

    // Engine implementation

    /**
     * Default constructor.
     */
    public FlowsMan() {
        changeStatus(EngineStatus.STANDBY);
    }

    public String getSessionTag() {
        return sessionTag;
    }

    public void setSessionTag(String sessionTag) {
        if (_status == EngineStatus.STANDBY) {
            this.sessionTag = sessionTag;
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    //      STANDBY inputs (proper)

    /**
     * Adds a device to the enumeration, this is to be used before the {@code start} call, before the internal IO-map rendering.
     *
     * @param node Device to add.
     */
    @Override
    public void addInput(NodePlugin<TimeT, ValueT> node) {
        if (_status == EngineStatus.STANDBY) {
            // Check if only the name is already contained
            if (!_userDevices.containsKey(node.getName())) {
                _userDevices.put(node.getName(), new NodeDecorator<>(node, this));
                for (SensorComponent<TimeT, ValueT> s : node.getSensors())
                    s.registerManager(this);
            }
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    @Override
    public NodePlugin<TimeT, ValueT> getInput(String name) {
        Object r = _userDevices.get(name);
        //noinspection unchecked
        return r == null ? null : ((NodeDecorator)r).getPlugIn();
    }

    /**
     * Adds a link between a sensor and an output (N to M relation) before the {@code start} call.
     *
     * @param fromSensor Input sensor retreived from a device.
     * @param toOutput   Output channel.
     */
    @Override
    public void addLink(SensorComponent<TimeT, ValueT> fromSensor, OutputPlugin<TimeT, ValueT> toOutput) {
        // Manual indexOf for performance
        for (OutputDecorator<TimeT, ValueT> outMan : _userOutputs.values())
            if (toOutput == outMan.getPlugIn()) { // for reference, safe
                addLink(fromSensor, outMan);
                break;
            }
    }

    @Override
    public void setOutputEnabled(boolean enabled, String name) {
        if (_userOutputs.containsKey(name)) {
            ((OutputDecorator)_userOutputs.get(name)).setEnabled(enabled);
        }
    }

    @Override
    public boolean getOutputEnabled(String name) {
        return _userOutputs.containsKey(name) && ((OutputDecorator)_userOutputs.get(name)).isEnabled();
    }

    /**
     * Adds a link between a sensor and an output-decorator object (N to M relation) before the {@code start} call.
     *
     * @param fromSensor Input sensor retrieved from a device.
     * @param outMan     OutputDecorator object.
     */
    void addLink(SensorComponent<TimeT, ValueT> fromSensor, OutputDecorator<TimeT, ValueT> outMan) {
        if (_status == EngineStatus.STANDBY) {
            fromSensor.addOutput(outMan);
            outMan.addSensor(fromSensor);
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    /**
     * Adds an output to the enumeration, this is to be used before the {@code start} call, before the internal in-out map rendering.
     *
     * @param output Output to add.
     */
    @Override
    public void addOutput(OutputPlugin<TimeT, ValueT> output) {
        if (_status == EngineStatus.STANDBY) {
            // Check if only the name is already contained
            if (!_userOutputs.containsKey(output.getName()))
                _userOutputs.put(output.getName(), new OutputDecorator<>(output, this));
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    @Override
    public OutputPlugin<TimeT, ValueT> getOutput(String name) {
        Object r = _userOutputs.get(name);
        //noinspection unchecked
        return r == null ? null : ((OutputDecorator)r).getPlugIn();
    }

    //      STANDBY aux gets (proper)

    /**
     * Enumerates every Device managed.
     *
     * @return Enumerator usable trough a for (INode d : enumerator)
     */
    @Override
    public Iterable<NodePlugin<TimeT, ValueT>> getDevices() {
        return new Iterable<NodePlugin<TimeT, ValueT>>() {
            @Override
            public Iterator<NodePlugin<TimeT, ValueT>> iterator() {
                final Iterator<NodeDecorator<TimeT, ValueT>> i = _userDevices.values().iterator();
                return new Iterator<NodePlugin<TimeT, ValueT>>() {

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public NodePlugin<TimeT, ValueT> next() {
                        return i.next().getPlugIn();
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
    @Override
    public Iterable<OutputPlugin<TimeT, ValueT>> getOutputs() {
        return new Iterable<OutputPlugin<TimeT, ValueT>>() {
            @Override
            public Iterator<OutputPlugin<TimeT, ValueT>> iterator() {
                final Iterator<OutputDecorator<TimeT, ValueT>> i = _userOutputs.values().iterator();
                return new Iterator<OutputPlugin<TimeT, ValueT>>() {

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public OutputPlugin<TimeT, ValueT> next() {
                        return i.next().getPlugIn();
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
     * @param device {@code INode} to initializeNode
     */
    void initialize(NodeDecorator device) {
        // The connection state is checked before the start end callback.
        //noinspection StatementWithEmptyBody
        if (/*_decDevices.contains(device) &&  */device.getStatus() == DeviceStatus.NOT_INITIALIZED) {
            device.initializeNode();
        } else {
//            Log.w(LOG_TAG, "INode not NOT_INITIALIZED: " + device.toString());
        }
    }

    /**
     * This method allows to initializeNode the device before the {@code start} call.
     * Made private
     *
     * @param output {@code IOutput} to finalize.
     */
    void initialize(OutputDecorator<TimeT, ValueT> output, String sessionName) {
        //noinspection StatementWithEmptyBody
        if (/*_decOutputs.contains(output) &&  */output.getStatus() == OutputStatus.NOT_INITIALIZED) {
            output.initializeOutput(sessionName);
        } else {
//            Log.w(LOG_TAG, "IOutput not NOT_INITIALIZED: " + output.toString());
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Made private
     *
     * @param device {@code INode} to finalize.
     */
    void finalize(NodeDecorator device) {
        // The connection state is not checked
        //noinspection StatementWithEmptyBody
        if (/*_decDevices.contains(device) &&  */device.getStatus() == DeviceStatus.INITIALIZED) {
            device.finalizeNode();
        } else {
//            Log.w(LOG_TAG, "INode not INITIALIZED: " + device.toString());
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Made private
     *
     * @param output {@code IOutput} to finalize.
     */
    void finalize(IOutput<TimeT, ValueT> output) {
        //noinspection StatementWithEmptyBody
        if (/*_decOutputs.contains(output) &&  */output.getStatus() == OutputStatus.INITIALIZED) {
            output.finalizeOutput();
        } else {
//            Log.w(LOG_TAG, "IOutput not INITIALIZED: " + output.toString());
        }
    }

    //      Engine operation

    public void setLinkMode(LinkMode mode) {
        if (_status == EngineStatus.STANDBY)
            _linkMode = mode;
        else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    /**
     * Renders the IO-mapping and in two times (async.) initializes the devices and the outputs.
     * <p/>
     * If a device/output was initialized before this call and it is not already INITIALIZED the
     * engine will wait for it for an indefinite timestamp. In this period the engine status will stay
     * {@code EngineStatus.PREPARING}.
     * <p/>
     * The session name is the date-timestamp string {@code Long.toString(System.currentTimeMillis())}
     * if the sessionTag has not been set.
     */
    @SuppressWarnings("JavaDoc")
    @Override
    public void start() {
        if (sessionTag == null || sessionTag.length() == 0)
            sessionTag = Long.toString(System.currentTimeMillis());
        start(sessionTag);
    }

    /**
     * Renders the IO-mapping and in two times (async.) initializes the devices and the outputs.
     * <br/>
     * If a device/output was initialized before this call and it is not already INITIALIZED the
     * engine will wait for it for an indefinite time. In this period the engine status will stay
     * {@code EngineStatus.PREPARING}.
     * <p/>
     * Allows to give a name to the current session but it DOES NOT CHECK if it already exists.
     */
    public void start(String sessionName) {
        if (getStatus() == EngineStatus.STANDBY
                || getStatus() == EngineStatus.CLOSED) {
            // Prepares the links
            switch (_linkMode) {
                case PRODUCT:
                    // SENSORS x OUTPUTS
                    for (NodeDecorator<TimeT, ValueT> d : _userDevices.values())
                        for (SensorComponent<TimeT, ValueT> s : d.getSensors())      // FOREACH SENSOR
                            for (OutputDecorator<TimeT, ValueT> o : _userOutputs.values())    // LINK TO EACH OUTPUT
                                addLink(s, o);
                    break;
                case NTH_TO_NTH:
                    // max SENSORS, OUTPUTS
                    int maxi = Math.max(_userDevices.size(), _userOutputs.size());
                    for (int i = 0; i < maxi; i++)                                                                      // FOREACH OF THE LONGEST
                        for (SensorComponent<TimeT, ValueT> s : new ArrayList<>(_userDevices.values()).get(i % _userDevices.size()).getSensors())      // LINK MODULE LOOPING ON THE SHORTEST
                            addLink(s, new ArrayList<>(_userOutputs.values()).get(i % _userOutputs.size()));
                    break;
            }
            changeStatus(EngineStatus.PREPARING);
            _devicesToInit.addAll(_userDevices.values());
            // Launches the initializations
            for (NodeDecorator d : _userDevices.values()) {
                // only if NOT_INITIALIZED: checked in the initializeNode method
                initialize(d);
            }
            _outputsToInit.addAll(_userOutputs.values());
            for (OutputDecorator<TimeT, ValueT> o : _userOutputs.values()) {
                // only if NOT_INITIALIZED: checked in the initializeNode method
                initialize(o, sessionName);
            }
            // WAS _outputsSensors.clear();
            // WAS _outputsSensors = null;
        } else
            throw new UnsupportedOperationException("Engine already running!");
    }

    /**
     * Returns weather the global streaming is paused.
     *
     * @return Boolean value.
     */
    @Override
    public boolean isPaused() {
        return _paused;
    }

    /**
     * Allows to pause or to resume the streaming in the faster way.
     *
     * @param paused Boolean value.
     */
    @Override
    public void setPaused(boolean paused) {
        _paused = paused;
    }

    protected void changeStatus(EngineStatus status) {
        _status = status;
        if (_onStatusChanged != null)
            _onStatusChanged.handle(this, _status);
    }

    /**
     * Gets the status of the engine.
     *
     * @return The actual status of the engine.
     */
    @Override
    public EngineStatus getStatus() {
        return _status;
    }

    /**
     * This method finalizes every device and every output and waits the queues to get empty.
     */
    @Override
    public void stop() {
        changeStatus(EngineStatus.FINALIZING);
        for (NodeDecorator d : _userDevices.values()) {
            // only if INITIALIZED: checked in the method
            finalize(d);
        }
        for (IOutput<TimeT, ValueT> o : _userOutputs.values()) {
            // only if INITIALIZED: checked in the method
            finalize(o);
        }
        changeStatus(EngineStatus.FINALIZED);
    }

    /**
     *
     */
    @Override
    public void close() {
        switch (getStatus()) {
            case STANDBY:
                break;
            case STREAMING:
                stop();
            case FINALIZED:
                changeStatus(EngineStatus.CLOSING);
                for (NodeDecorator<TimeT, ValueT> d : _userDevices.values())
                    for (SensorComponent<TimeT, ValueT> s : d.getPlugIn().getSensors())
                        s.close();
                for (IOutput<TimeT, ValueT> o : _userOutputs.values())
                o.close();
                changeStatus(EngineStatus.CLOSED);
                break;
            case CLOSED:
                break;
            default:
                throw new UnsupportedOperationException(
                        "Another operation is currently trying to chenge the state: " +
                                getStatus().toString());
        }
    }

        /**
     * Finalizes the object calling also the {@code close} method.
     *
     * @throws Throwable
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
    @Override
    public void setOnStatusChanged(EventCallback<IUserInterface<NodePlugin<TimeT, ValueT>, SensorComponent<TimeT, ValueT>, OutputPlugin<TimeT, ValueT>>, EngineStatus> callback) {
        _onStatusChanged = callback;
    }

    /**
     * Sets a listener to receive every device's state change.
     *
     * @param callback Callback to call when any device's state changes.
     */
    @Override
    public void setOnDeviceStatusChanged(EventCallback<NodePlugin<TimeT, ValueT>, DeviceStatus> callback) {
        _onDeviceStatusChanged = callback;
    }

    /**
     * Sets a listener to receive every output's state change.
     *
     * @param callback Callback to call when any device's state changes.
     */
    @Override
    public void setOnOutputStatusChanged(EventCallback<OutputPlugin<TimeT, ValueT>, OutputStatus> callback) {
        _onOutputStatusChanged = callback;
    }
}
