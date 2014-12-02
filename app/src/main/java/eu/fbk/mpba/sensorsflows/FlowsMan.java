package eu.fbk.mpba.sensorsflows;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import eu.fbk.mpba.sensorsflows.base.Booleaned;
import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.EventCallback;
import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.base.IDevice;
import eu.fbk.mpba.sensorsflows.base.IDeviceCallback;
import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.IOutputCallback;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.ISensorCallback;
import eu.fbk.mpba.sensorsflows.base.IUserInterface;
import eu.fbk.mpba.sensorsflows.base.OutputStatus;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.IterToEnum;

/**
 * FlowsMan is the class that represents the engine of the library.
 * This is the only interface that the user should use.
 * Implementation of the IUserInterface
 * @param <TimeT> The type of the time returned by the outputs (must be the same for every item).
 * @param <ValueT> The type of the value returned by the devices (must be the same for every item).
 */
public class FlowsMan<TimeT, ValueT> implements
        IUserInterface<TimeT, ValueT>, IDeviceCallback<IDevice>,
        ISensorCallback<ISensor, TimeT, ValueT>, IOutputCallback<TimeT, ValueT> {

    // Extra Interfaces

    /**
     * Not for the end-user.
     * @param sender sender
     * @param state arg
     */
    @Override public void deviceStateChanged(IDevice sender, DeviceStatus state) {
        if (state == DeviceStatus.INITIALIZED) {
            synchronized (_itemsToInitLock) {
                if (_devicesToInit.contains(sender)) {
                    _devicesToInit.remove(sender);
                    if (_status == EngineStatus.PREPARING && _devicesToInit.isEmpty()) {
                        // POI Change point
                        _devicesToInit = null;
                        if (_outputsToInit == null)
                            changeState(EngineStatus.STREAMING);
                    }
                }
            }
        }
    }

    /**
     * Not for the end-user.
     * @param sender sender
     * @param state arg
     */
    @Override public void outputStateChanged(IOutput<TimeT, ValueT> sender, OutputStatus state) {
        if (state == OutputStatus.INITIALIZED) {
            synchronized (_itemsToInitLock) {
                if (_outputsToInit.contains(sender)) {
                    _outputsToInit.remove(sender);
                    if (_status == EngineStatus.PREPARING && _outputsToInit.isEmpty()) {
                        // POI Change point
                        _outputsToInit = null;
                        if (_devicesToInit == null)
                            changeState(EngineStatus.STREAMING);
                    }
                }
            }
        }
    }

    /**
     * Not for the end-user.
     * @param sender sender
     * @param state arg
     */
    @Override public void sensorStateChanged(ISensor sender, SensorStatus state) {

    }

    /**
     * Not for the end-user.
     * @param sender sender
     * @param time timestamp
     * @param value value
     */
    @Override public void pushData(ISensor sender, TimeT time, ValueT value) {

    }

    /**
     * Not for the end-user.
     * @param sender sender
     * @param type event code
     * @param message message text
     */
    @Override public void deviceEvent(IDevice sender, int type, String message) {

    }

    /**
     * Not for the end-user.
     * @param sender sender
     * @param type event code
     * @param message message text
     */
    @Override public void sensorEvent(ISensor sender, int type, String message) {

    }


    // Fields

    final String LOG_TAG = "ALE SFW";
    final String _itemsToInitLock = "_itemsToInitLock";

    protected EngineStatus _status = EngineStatus.STANDBY;

    protected EventCallback<IUserInterface<TimeT, ValueT>, EngineStatus> _onStateChanged = null;    // null
    protected EventCallback<IDevice, DeviceStatus> _onDeviceStateChanged = null;                    // null
    protected EventCallback<IOutput<TimeT, ValueT>, OutputStatus> _onOutputStateChanged = null;     // null

    protected Set<IDevice> _userDevices = new HashSet<IDevice>();
    protected Set<IOutput<TimeT, ValueT>> _userOutputs = new HashSet<IOutput<TimeT, ValueT>>();
    protected ArrayList<Pair<ISensor, Booleaned<IOutput<TimeT, ValueT>>>> _userLinks;

    protected Set<IDevice> _devicesToInit = new HashSet<IDevice>(); // null
    protected Set<IOutput> _outputsToInit = new HashSet<IOutput>(); // null

    protected Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>> _linksMap = new Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>>();
    protected Hashtable<ISensor, Boolean> _sensorsListenage = new Hashtable<ISensor, Boolean>();
    protected Map<IOutput, Queue<Pair<TimeT, ValueT>>> _outputQueues = new Hashtable<IOutput, Queue<Pair<TimeT, ValueT>>>();


    // Engine implementation

    /**
     * Default constructor.
     */
    public FlowsMan() {
        changeState(EngineStatus.STANDBY);
    }

    /**
     * Adds a device to the enumeration, this is to be used before the {@code start} call, before the internal IO-map rendering.
     * @param device Device to add.
     */
    @Override public void addDevice(IDevice device) {
        if (_status == EngineStatus.STANDBY)
            _userDevices.add(device);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    /**
     * Adds a link to the links that will be mapped in the IO-map when {@code start} will be called.
     * @param fromSensor Input sensor retreived from a device.
     * @param toOutput Output channel.
     * @param initialEnabledState Initial enabling state of the link.
     */
    @Override public void addLink(ISensor fromSensor, IOutput<TimeT, ValueT> toOutput, boolean initialEnabledState) {
        _userLinks.add(new Pair<ISensor, Booleaned<IOutput<TimeT, ValueT>>>(fromSensor, new Booleaned<IOutput<TimeT, ValueT>>(toOutput, initialEnabledState)));
    }

    /**
     * Adds an output to the enumeration, this is to be used before the {@code start} call, before the internal in-out map rendering.
     * @param output Output to add.
     */
    @Override public void addOutput(IOutput<TimeT, ValueT> output) {
        if (_status == EngineStatus.STANDBY)
            _userOutputs.add(output);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    /**
     * Enumerates every Device managed.
     * @return Enumerator usable trough a for (IDevice... : ...)...
     */
    @Override public Enumeration<IDevice> getDevices() {
        return new IterToEnum<IDevice>(_userDevices.iterator());
    }

    /**
     * Enumerates every Output managed.
     * @return Enumerator usable trough a for (IOutput... : ...)...
     */
    @Override public Enumeration<IOutput<TimeT, ValueT>> getOutputs() {
        return new IterToEnum<IOutput<TimeT, ValueT>>(_userOutputs.iterator());
    }

    // TODO 6 Add async end-user end on the public callbacks before the start
    /**
     * This method allows to initialize the device before the {@code start} call.
     * Should not be so useful.
     * @param device {@code IDevice} to initialize
     */
    @Override public void initialize(IDevice device) {
        // The connection state is checked before the start end callback.
        if (_userDevices.contains(device) &&  device.getState() == DeviceStatus.NOT_INITIALIZED) {
            device.initialize();
        }
        else {
            throw new NoSuchElementException("IDevice not present in the collection.");
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Should not be so useful.
     * @param device {@code IDevice} to finalize.
     */
    @Override public void finalize(IDevice device) {
        // The connection state is not checked
        if (_userDevices.contains(device) &&  device.getState() == DeviceStatus.INITIALIZED) {
            device.finalizeDevice();
        }
        else {
            throw new NoSuchElementException("IDevice not present in the collection.");
        }
    }

    /**
     * This method allows to initialize the device before the {@code start} call.
     * Should not be so useful.
     * @param output {@code IOutput} to finalize.
     */
    @Override public void initialize(IOutput<TimeT, ValueT> output) {
        if (_userOutputs.contains(output) &&  output.getState() == OutputStatus.NOT_INITIALIZED) {
            output.initialize();
        }
        else {
            throw new NoSuchElementException("IOutput not present in the collection.");
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Should not be so useful.
     * @param output {@code IOutput} to finalize.
     */
    @Override public void finalize(IOutput<TimeT, ValueT> output) {
        if (_userOutputs.contains(output) &&  output.getState() == OutputStatus.INITIALIZED) {
            output.finalizeOutput();
        }
        else {
            throw new NoSuchElementException("IOutput not present in the collection.");
        }
    }

    /**
     * This method asks to the device to switch on a sensor.
     * Works if the engine is in {@code EngineStatus.STREAMING} or in
     * {@code EngineStatus.PAUSED} state.
     * @param sensor {@code ISensor} to switch on.
     */
    @Override public void switchOn(ISensor sensor) {
        // Note the difference with the set streaming
        if (_status == EngineStatus.STREAMING && _userDevices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Switching on async " + sensor.toString());
            sensor.switchOnAsync();
        }
        else {
            throw new NoSuchElementException("ISensor not present in the collection.");
        }
    }

    /**
     * This method asks to the device to switch off a sensor.
     * Works if the engine is in {@code EngineStatus.STREAMING} or in
     * {@code EngineStatus.PAUSED} state.
     * @param sensor {@code ISensor} to switch off.
     */
    @Override public void switchOff(ISensor sensor) {
        // Note the difference with the set streaming
        if (_status == EngineStatus.STREAMING && _userDevices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Switching off async " + sensor.toString());
            sensor.switchOffAsync();
        }
        else {
            throw new NoSuchElementException("ISensor not present in the collection.");
        }
    }

    /**
     * Sets weather the engine should receive the data from the sensor or not.
     * This feature is useful if it is needed to start an acquisition with every sensor ready and
     * streaming without data holes caused by the fact that a sensor begins streaming before
     * another.
     * @param sensor The sensor.
     * @param streaming If to listen to the data events of the sensor.
     */
    @Override public void setSensorListened(ISensor sensor, boolean streaming) {
        /*if (_userLinks.contains(link)) {
            Log.v(LOG_TAG, "Setting streaming " + (streaming ? "ON " : "OFF ") + link.toString());
            link.setEnabled(streaming);
        }
        else {
            // T ODO 6 Maybe an exception
            Log.v(LOG_TAG, "Refused to initialize " + link.toString());
        }*/
        if (_sensorsListenage.containsKey(sensor))
            _sensorsListenage.put(sensor, streaming);
        else
            throw new NoSuchElementException("ISensor not present in the collection.");
    }

    /**
     * Gets weather the engine should receive the data from the sensor or not.
     * This feature is useful if it is needed to start an acquisition with every sensor ready and
     * streaming without data holes caused by the fact that a sensor begins streaming before
     * another.
     * @param sensor The sensor.
     * @return If the sensor is listened.
     */
    @Override public boolean isSensorListened(ISensor sensor) {
        /*if (_userLinks.contains(link)) {
            return link.getEnabled();
        }
        else {
            Log.v(LOG_TAG, "Link not contained " + link.toString());
            // T ODO 6 Maybe an exception
            return false;
        }*/
        // Can throw NullPointer in get in hashCode
        Boolean x = _sensorsListenage.get(sensor);
        if (x != null)
            return x;
        else
            throw new NoSuchElementException("ISensor not present in the collection.");
    }

    /**
     * Renders the IO-map and sequentially initializes the devices and the outputs.
     * If a device/output was initialized asyncronously before this call and it is not already
     * INITIALIZED the engine will wait for it for an indefinite time. In this period the engine
     * status will stay {@code EngineStatus.PREPARING}.
     */
    @Override public void start() {
        changeState(EngineStatus.PREPARING);
        renderTheMap();
        for (IDevice d : _userDevices) {
            // only if NOT_INITIALIZED: checked in the initialize method
            initialize(d);
        }
        for (IOutput<TimeT, ValueT> o : _userOutputs) {
            // only if NOT_INITIALIZED: checked in the initialize method
            initialize(o);
        }
    }

    protected void renderTheMap() {
        // Render the links map
        // Build inputs
        //     put an entry for each sensor (for each device)
        for (IDevice d : _userDevices) {
            for (ISensor s : d.getSensors()){
                _linksMap.put(s, new ArrayList<Booleaned<IOutput<TimeT, ValueT>>>());
                _sensorsListenage.put(s, false);
            }
        }
        // Set outputs
        //     put an output in the corresponding input's entry list for each link specified
        for (Pair<ISensor, Booleaned<IOutput<TimeT, ValueT>>> l : _userLinks) {
            _linksMap.get(l.first).add(new Booleaned<IOutput<TimeT, ValueT>>(l.second.getTheOther(), l.second.isTrue()));
        }
    }

    /**
     * Returns weather the global streaming is paused.
     * @return Boolean value.
     */
    @Override public boolean isPaused() {
        return _status == EngineStatus.PAUSED;
    }

    /**
     * Allows to pause or to resume the streaming in the faster way.
     * @param paused Boolean value.
     */
    @Override public void setPaused(boolean paused) {
        if (_status == EngineStatus.STREAMING || _status == EngineStatus.PAUSED)
            _status = paused ? EngineStatus.PAUSED : EngineStatus.STREAMING;
    }

    /**
     * This method finalizes every device and every output and prepares the instance to be trashed.
     */
    @Override public void close() {
        for (IDevice d : _userDevices) {
            // only if INITIALIZED: checked in the method
            finalize(d);
        }
        for (IOutput<TimeT, ValueT> o : _userOutputs) {
            // only if INITIALIZED: checked in the method
            finalize(o);
        }
    }

    protected void changeState(EngineStatus status) {
        _status = status;
        if (_onStateChanged != null)
            _onStateChanged.handle(this, _status);
    }

    /**
     * Sets a listener to receive the engine state changes.
     * @param callback Callback to call when the engine state changes.
     */
    @Override public void setOnStateChanged(EventCallback<IUserInterface<TimeT, ValueT>, EngineStatus> callback) {
        _onStateChanged = callback;
    }

    /**
     * Sets a listener to receive every device's state change.
     * @param callback Callback to call when any device's state changes.
     */
    @Override public void setOnDeviceStateChanged(EventCallback<IDevice, DeviceStatus> callback) {
        _onDeviceStateChanged = callback;
    }

    /**
     * Sets a listener to receive every output's state change.
     * @param callback Callback to call when any device's state changes.
     */
    @Override public void setOnOutputStateChanged(EventCallback<IOutput<TimeT, ValueT>, OutputStatus> callback) {
        _onOutputStateChanged = callback;
    }

    /**
     * Gets the status of the engine.
     * @return The actual status of the engine.
     */
    @Override public EngineStatus getStatus() {
        return _status;
    }

    /**
     * Finalizes the object calling also the {@code close} method.
     * @throws Throwable
     */
    @Override protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
