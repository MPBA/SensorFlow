package eu.fbk.mpba.sensorsflows;

import android.util.Log;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.EventCallback;
import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.base.IDeviceCallback;
import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.IOutputCallback;
import eu.fbk.mpba.sensorsflows.base.ISensorDataCallback;
import eu.fbk.mpba.sensorsflows.base.IUserInterface;
import eu.fbk.mpba.sensorsflows.base.OutputStatus;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * FlowsMan is the class that represents the engine of the library.
 * This is the only interface that the user should use.
 * Implementation of the IUserInterface
 * @param <TimeT> The type of the time returned by the outputs (must be the same for every item).
 * @param <ValueT> The type of the value returned by the devices (must be the same for every item).
 */
public class FlowsMan<TimeT, ValueT> implements
        IUserInterface<DeviceImpl, SensorImpl<TimeT, ValueT>, TimeT, ValueT>, IDeviceCallback<DeviceImpl>,
        ISensorDataCallback<SensorImpl, TimeT, ValueT>, IOutputCallback<TimeT, ValueT> {

    // Status Interface

    /**
     * Not for the end-user.
     *
     * @param sender sender
     * @param state  arg
     */
    @Override
    public void deviceStateChanged(DeviceImpl sender, DeviceStatus state) {
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
                // FIXME WARN User-code time dependency in the output thread or son
                changeState(EngineStatus.STREAMING);
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
    public void outputStateChanged(IOutput<TimeT, ValueT> sender, OutputStatus state) {
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
                // FIXME WARN User-code time dependency in the output thread or son
                changeState(EngineStatus.STREAMING);
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
    public void sensorStateChanged(SensorImpl sender, TimeT time, SensorStatus state) {
        // TO DO The sensor has to send also an event on a status change.
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
    public void sensorValue(SensorImpl sender, TimeT time, ValueT value) {
        if (sender.isListened()) {
            for (Object o : sender.getOutputs()) {
                //noinspection unchecked
                ((OutputImpl<TimeT, ValueT>)o).sensorValue(sender, time, value);
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
    public void sensorEvent(SensorImpl sender, TimeT time, int type, String message) {
        if (sender.isListened()) {
            for (Object o : sender.getOutputs()) {
                //noinspection unchecked
                ((OutputImpl<TimeT, ValueT>)o).sensorEvent(sender, time, type, message);
            }
        }
    }

    //      no outputEvent for now

    // Fields

    final String LOG_TAG = "ALE SFW";
    final String _emAlreadyRendered = "The map is rendered. No inputs, outputs or links can be added at this time for now.";
    final String _itemsToInitLock = "_itemsToInitLock";

    protected EngineStatus _status = EngineStatus.STANDBY;

    protected Set<DeviceImpl> _userDevices = new HashSet<DeviceImpl>();
    protected Set<IOutput<TimeT, ValueT>> _userOutputs = new HashSet<IOutput<TimeT, ValueT>>();
    // WAS protected ArrayList<Pair<SensorImpl, Booleaned<IOutput<TimeT, ValueT>>>> _userLinks;    // null

    protected Set<DeviceImpl> _devicesToInit = new HashSet<DeviceImpl>();                                    // null
    protected Set<IOutput> _outputsToInit = new HashSet<IOutput>();                                       // null

    protected EventCallback<IUserInterface<DeviceImpl, SensorImpl<TimeT, ValueT>, TimeT, ValueT>
            , EngineStatus> _onStateChanged = null;                   // null
    protected EventCallback<DeviceImpl, DeviceStatus> _onDeviceStateChanged = null;                 // null
    protected EventCallback<IOutput<TimeT, ValueT>, OutputStatus> _onOutputStateChanged = null;     // null

    // WAS protected Hashtable<SensorImpl, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>> _linksMap = new Hashtable<SensorImpl, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>>();
    // WAS protected Hashtable<SensorImpl, Boolean> _sensorsListenage = new Hashtable<SensorImpl, Boolean>();
    // WAS protected Map<IOutput, Queue<Pair<TimeT, ValueT>>> _outputQueues = new Hashtable<IOutput, Queue<Pair<TimeT, ValueT>>>();


    // Engine implementation

    /**
     * Default constructor.
     */
    public FlowsMan() {
        changeState(EngineStatus.STANDBY);
    }

    //      STANDBY inputs (proper)

    /**
     * Adds a device to the enumeration, this is to be used before the {@code start} call, before the internal IO-map rendering.
     *
     * @param device Device to add.
     */
    @Override
    public void addDevice(DeviceImpl device) {
        if (_status == EngineStatus.STANDBY)
            _userDevices.add(device);
        else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    /**
     * Adds a link between a sensor and an output (N to M relation) before the {@code start} call.
     *
     * @param fromSensor Input sensor retreived from a device.
     * @param toOutput   Output channel.
     */
    @Override
    public void addLink(SensorImpl<TimeT, ValueT> fromSensor, IOutput<TimeT, ValueT> toOutput) {
        if (_status == EngineStatus.STANDBY) {
            // WAS _userLinks.add(new Pair<SensorImpl, Booleaned<IOutput<TimeT, ValueT>>>(fromSensor, new Booleaned<IOutput<TimeT, ValueT>>(toOutput, initialEnabledState)));
            // TODO N1 Remember enabling/disabling each link
            // FIXME WARN Unchecked cast
            fromSensor.addOutput((OutputImpl<TimeT, ValueT>)toOutput);
        } else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    /**
     * Adds an output to the enumeration, this is to be used before the {@code start} call, before the internal in-out map rendering.
     *
     * @param output Output to add.
     */
    @Override
    public void addOutput(IOutput<TimeT, ValueT> output) {
        if (_status == EngineStatus.STANDBY)
            _userOutputs.add(output);
        else
            throw new UnsupportedOperationException(_emAlreadyRendered);
    }

    //      STANDBY aux gets (proper)

    /**
     * Enumerates every Device managed.
     *
     * @return Enumerator usable trough a for (IDevice d : enumerator)
     */
    @Override
    public Iterable<DeviceImpl> getDevices() {
        return new ReadOnlyIterable<DeviceImpl>(_userDevices.iterator());
    }

    /**
     * Enumerates every Output managed.
     *
     * @return Enumerator usable trough a for (IOutput o : enumerator)
     */
    @Override
    public Iterable<IOutput<TimeT, ValueT>> getOutputs() {
        return new ReadOnlyIterable<IOutput<TimeT, ValueT>>(_userOutputs.iterator());
    }

    //      Internal init and final management

    /**
     * This method allows to initialize the device before the {@code start} call.
     * Made private
     *
     * @param device {@code IDevice} to initialize
     */
    void initialize(DeviceImpl device) {
        // The connection state is checked before the start end callback.
        if (/*_userDevices.contains(device) &&  */device.getState() == DeviceStatus.NOT_INITIALIZED) {
            device.initialize();
        } else {
            Log.w(LOG_TAG, "IDevice not NOT_INITIALIZED: " + device.toString());
        }
    }

    /**
     * This method allows to initialize the device before the {@code start} call.
     * Made private
     *
     * @param output {@code IOutput} to finalize.
     */
    void initialize(IOutput<TimeT, ValueT> output) {
        if (/*_userOutputs.contains(output) &&  */output.getState() == OutputStatus.NOT_INITIALIZED) {
            output.initialize();
        } else {
            Log.w(LOG_TAG, "IOutput not NOT_INITIALIZED: " + output.toString());
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Made private
     *
     * @param device {@code IDevice} to finalize.
     */
    void finalize(DeviceImpl device) {
        // The connection state is not checked
        if (/*_userDevices.contains(device) &&  */device.getState() == DeviceStatus.INITIALIZED) {
            device.finalizeDevice();
        } else {
            Log.w(LOG_TAG, "IDevice not INITIALIZED: " + device.toString());
        }
    }

    /**
     * This method allows to finalize the device before the {@code close} call.
     * Made private
     *
     * @param output {@code IOutput} to finalize.
     */
    void finalize(IOutput<TimeT, ValueT> output) {
        if (/*_userOutputs.contains(output) &&  */output.getState() == OutputStatus.INITIALIZED) {
            output.finalizeOutput();
        } else {
            Log.w(LOG_TAG, "IOutput not INITIALIZED: " + output.toString());
        }
    }

    //      ACTIVE operation commands (proper, but public in the implementations)

    /**
     * This method asks to the device to switch on a sensor.
     * Works if the engine is in {@code EngineStatus.STREAMING} or in
     * {@code EngineStatus.PAUSED} state.
     *
     * @param sensor {@code ISensor} to switch on.
     */
    @Override
    public void switchOn(SensorImpl<TimeT, ValueT> sensor) {
        // Note the difference with the set streaming
        if ((_status == EngineStatus.STREAMING ||
                _status == EngineStatus.PAUSED) &&
                _userDevices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Switching on async " + sensor.toString());
            sensor.switchOnAsync();
        } else {
            throw new NoSuchElementException("ISensor not present in the collection.");
        }
    }

    /**
     * This method asks to the device to switch off a sensor.
     * Works if the engine is in {@code EngineStatus.STREAMING} or in
     * {@code EngineStatus.PAUSED} state.
     *
     * @param sensor {@code ISensor} to switch off.
     */
    @Override
    public void switchOff(SensorImpl<TimeT, ValueT> sensor) {
        // Note the difference with the set streaming
        if ((_status == EngineStatus.STREAMING ||
                _status == EngineStatus.PAUSED) &&
                _userDevices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Switching off async " + sensor.toString());
            sensor.switchOffAsync();
        } else {
            throw new NoSuchElementException("ISensor not present in the collection.");
        }
    }

    //      ACTIVE operation commands (improper, public in the implementations)

    /**
     * Sets weather the engine should receive the data from the sensor or not.
     * This feature is useful if it is needed to start an acquisition from a sensor with a low start
     * lag as before the start the sensor is active but simply the data is not notified.
     *
     * @param sensor    The sensor.
     * @param streaming If to listen to the data events of the sensor.
     */
    @Override
    public void setSensorListened(SensorImpl<TimeT, ValueT> sensor, boolean streaming) {
        if (_userDevices.contains(sensor.getParentDevice()))
            sensor.setListened(streaming);
        else
            throw new NoSuchElementException("ISensor not present in the collection.");
    }

    /**
     * Gets weather the engine should receive the data from the sensor or not.
     * This feature is useful if it is needed to start an acquisition from a sensor with a low start
     * lag as before the start the sensor is active but simply the data is not notified.
     *
     * @param sensor The sensor.
     * @return If the sensor is listened.
     */
    @Override
    public boolean isSensorListened(SensorImpl<TimeT, ValueT> sensor) {
        if (_userDevices.contains(sensor.getParentDevice()))
            return sensor.isListened();
        else
            throw new NoSuchElementException("ISensor not present in the collection.");
    }

    //      Engine operation

    /**
     * Renders the IO-mapping and in two times (async.) initializes the devices and the outputs.
     * <p/>
     * If a device/output was initialized before this call and it is not already INITIALIZED the
     * engine will wait for it for an indefinite time. In this period the engine status will stay
     * {@code EngineStatus.PREPARING}.
     */
    @Override
    public void start() {
        changeState(EngineStatus.PREPARING);
        // WAS renderTheMap();
        // Launches the initializations
        for (DeviceImpl d : _userDevices) {
            // only if NOT_INITIALIZED: checked in the initialize method
            initialize(d);
        }
        for (IOutput<TimeT, ValueT> o : _userOutputs) {
            // only if NOT_INITIALIZED: checked in the initialize method
            initialize(o);
        }
    }

    // WAS
    //protected void renderTheMap() {
    //    // Render the links map
    //    // Build inputs
    //    //     put an entry for each sensor (for each device)
    //    for (DeviceImpl d : _userDevices) {
    //        for (SensorImpl s : d.getSensors()){
    //            ArrayList<IOutput<TimeT, ValueT>> l = new ArrayList<IOutput<TimeT, ValueT>>();
    //            for (Pair<SensorImpl, Booleaned<IOutput<TimeT, ValueT>>> l : _userLinks) {
    //                // TO DO 8 Is the pointer comparison logically correct? (For now it is the same instance else implement the equals)
    //                if (l.first == s)
    //
    //            }
    //        }
    //    }
    //}

    /**
     * Returns weather the global streaming is paused.
     *
     * @return Boolean value.
     */
    @Override
    public boolean isPaused() {
        return _status == EngineStatus.PAUSED;
    }

    /**
     * Allows to pause or to resume the streaming in the faster way.
     *
     * @param paused Boolean value.
     */
    @Override
    public void setPaused(boolean paused) {
        if (_status == EngineStatus.STREAMING || _status == EngineStatus.PAUSED)
            _status = paused ? EngineStatus.PAUSED : EngineStatus.STREAMING;
    }

    protected void changeState(EngineStatus status) {
        _status = status;
        if (_onStateChanged != null)
            _onStateChanged.handle(this, _status);
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
     * This method finalizes every device and every output and prepares the instance to be trashed.
     */
    @Override
    public void close() {
        for (DeviceImpl d : _userDevices) {
            // only if INITIALIZED: checked in the method
            finalize(d);
        }
        for (IOutput<TimeT, ValueT> o : _userOutputs) {
            // only if INITIALIZED: checked in the method
            finalize(o);
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
    public void setOnStateChanged(EventCallback<IUserInterface<DeviceImpl, SensorImpl<TimeT, ValueT>, TimeT, ValueT>, EngineStatus> callback) {
        _onStateChanged = callback;
    }

    /**
     * Sets a listener to receive every device's state change.
     *
     * @param callback Callback to call when any device's state changes.
     */
    @Override
    public void setOnDeviceStateChanged(EventCallback<DeviceImpl, DeviceStatus> callback) {
        _onDeviceStateChanged = callback;
    }

    /**
     * Sets a listener to receive every output's state change.
     *
     * @param callback Callback to call when any device's state changes.
     */
    @Override
    public void setOnOutputStateChanged(EventCallback<IOutput<TimeT, ValueT>, OutputStatus> callback) {
        _onOutputStateChanged = callback;
    }
}
