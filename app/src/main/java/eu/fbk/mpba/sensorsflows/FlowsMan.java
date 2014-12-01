package eu.fbk.mpba.sensorsflows;

import android.util.Log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Set;

import eu.fbk.mpba.sensorsflows.base.Booleaned;
import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
import eu.fbk.mpba.sensorsflows.base.EngineStatusCallback;
import eu.fbk.mpba.sensorsflows.base.EngineStatus;
import eu.fbk.mpba.sensorsflows.base.IDevice;
import eu.fbk.mpba.sensorsflows.base.IDeviceCallback;
import eu.fbk.mpba.sensorsflows.base.ILink;
import eu.fbk.mpba.sensorsflows.base.IOutput;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.ISensorCallback;
import eu.fbk.mpba.sensorsflows.base.IUserInterface;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;
import eu.fbk.mpba.sensorsflows.util.IterToEnum;

/**
 * FlowsMan is the class that manages the engine of the library.
 * This is the only interface that the user should use
 * Implementation of the IUserInterface
 */
public class FlowsMan<TimeT, ValueT> implements IUserInterface<TimeT, ValueT>, IDeviceCallback<IDevice>, ISensorCallback<ISensor, TimeT, ValueT> {

    // Extra Interfaces

    @Override public void deviceStateChanged(IDevice device, DeviceStatus state) {
        if (state == DeviceStatus.INITIALIZED) {
            boolean ended = false;
            synchronized (_devicesToConnectLock) {
                _devicesToConnect.remove(device);
                if (_status == EngineStatus.PREPARING && _devicesToConnect.isEmpty())
                    ended = true;
            }
            if (ended) {
                _devicesToConnect = null;
                changeState(EngineStatus.STREAMING);
            }
        }
    }

    @Override public void sensorStateChanged(ISensor sensor, SensorStatus state) {

    }

    @Override public void pushData(ISensor sensor, TimeT time, ValueT value) {

    }

    @Override public void deviceEvent(IDevice device, int type, String message) {

    }

    @Override public void sensorEvent(ISensor sensor, int type, String message) {

    }


    // Fields

    final String LOG_TAG = "ALE SFW";
    final String _devicesToConnectLock = "Lock";

    protected EngineStatus _status = EngineStatus.STANDBY;
    protected boolean _globalStreaming = false;

    protected Set<IDevice> _devices = new HashSet<IDevice>();
    protected Set<IOutput<TimeT, ValueT>> _outputs = new HashSet<IOutput<TimeT, ValueT>>();

    protected Set<ILink<ISensor, IOutput<TimeT, ValueT>>> _userLinks = new HashSet<ILink<ISensor, IOutput<TimeT, ValueT>>>();

    protected Set<IDevice> _devicesToConnect = new HashSet<IDevice>();

    protected Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>> _linksMap = new Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>>();
    protected Hashtable<ISensor, Boolean> _sensorsListenage = new Hashtable<ISensor, Boolean>();

    EngineStatusCallback<IUserInterface<TimeT, ValueT>> _onStateChanged = null;


    // Engine implementation

    public FlowsMan() {
        changeState(EngineStatus.STANDBY);
    }

    @Override public void addDevice(IDevice device) {
        if (_status == EngineStatus.STANDBY)
            _devices.add(device);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    @Override public void addLink(ILink<ISensor, IOutput<TimeT, ValueT>> link) {
        if (_status == EngineStatus.STANDBY)
            _userLinks.add(link);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    public void addLink(final ISensor fromSensor, final IOutput<TimeT, ValueT> toOutput) {
        addLink(new ILink<ISensor, IOutput<TimeT, ValueT>>() {
            private boolean e = false;

            @Override public ISensor getInput() {
                return fromSensor;
            }

            @Override public IOutput<TimeT, ValueT> getOutput() {
                return toOutput;
            }

            @Override public boolean getEnabled() {
                return e;
            }

            @Override public void setEnabled(boolean enable) {
                e = enable;
            }
        });
    }

    @Override public void addOutput(IOutput<TimeT, ValueT> output) {
        if (_status == EngineStatus.STANDBY)
            _outputs.add(output);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    @Override public Enumeration<IDevice> getDevices() {
        return new IterToEnum<IDevice>(_devices.iterator());
    }

    @Override public Enumeration<IOutput<TimeT, ValueT>> getOutputs() {
        return new IterToEnum<IOutput<TimeT, ValueT>>(_outputs.iterator());
    }

    @Override public Enumeration<ILink<ISensor, IOutput<TimeT, ValueT>>> getLinks() {
        return new IterToEnum<ILink<ISensor, IOutput<TimeT, ValueT>>>(_userLinks.iterator());
    }

    /**
     * In this phase the device can be connected from the plug-in code or can also have already been
     * connected in its constructor.
     * @param device {@code IDevice} to initialize
     */
    @Override public void initialize(IDevice device) {
        // The connection state is checked before the start end callback.
        if (_devices.contains(device) &&  device.getState() == DeviceStatus.NOT_INITIALIZED) {
            device.connect();
            // TODO 6 Add async end-user end
        }
        else {
            throw new NoSuchElementException("IDevice not present in the collection.");
        }
    }

    @Override public void finalize(IDevice device) {
        // The connection state is not checked
        if (_devices.contains(device) &&  device.getState() == DeviceStatus.INITIALIZED) {
            device.close();
            // TODO 6 Add async end-user end
        }
        else {
            throw new NoSuchElementException("IDevice not present in the collection.");
        }
    }

    @Override public void switchOn(ISensor sensor) {
        // Note the difference with the set streaming
        if (_devices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Switching on async " + sensor.toString());
            sensor.switchOnAsync();
            // TODO 6 Add async end-user end
        }
        else {
            throw new NoSuchElementException("ISensor not present in the collection.");
        }
    }

    @Override public void switchOff(ISensor sensor) {
        // Note the difference with the set streaming
        if (_devices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Switching off async " + sensor.toString());
            sensor.switchOffAsync();
            // TODO 6 Add async end-user end
        }
        else {
            throw new NoSuchElementException("ISensor not present in the collection.");
        }
    }

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

    @Override public void start() {
        changeState(EngineStatus.PREPARING);
        renderTheMap();
        for (IDevice d : _devices) {
            // only if NOT_INITIALIZED: checked in the initialize method
            initialize(d);
        }
    }

    protected void renderTheMap() {
        // Render the links map
        // Build inputs
        //     put an entry for each sensor (for each device)
        for (IDevice d : _devices) {
            for (ISensor s : d.getSensors()){
                _linksMap.put(s, new ArrayList<Booleaned<IOutput<TimeT, ValueT>>>());
                _sensorsListenage.put(s, false);
            }
        }
        // Set outputs
        //     put an output in the corresponding input's entry list for each link specified
        for (ILink<ISensor, IOutput<TimeT, ValueT>> l : _userLinks) {
            _linksMap.get(l.getInput()).add(new Booleaned<IOutput<TimeT, ValueT>>(l.getOutput(), l.getEnabled()));
        }
    }

    @Override public boolean isPaused() {
        return _globalStreaming;
    }

    @Override public void setPaused(boolean status) {
        _globalStreaming = status;
    }

    @Override public void close() {
        for (IDevice d : _devices) {
            // only if INITIALIZED: checked in the method
            finalize(d);
        }
    }

    protected void changeState(EngineStatus status) {
        _status = status;
        if (_onStateChanged != null)
            _onStateChanged.handle(this, _status);
    }

    @Override public void setOnStateChanged(EngineStatusCallback<IUserInterface<TimeT, ValueT>> callback) {
        _onStateChanged = callback;
    }

    @Override public EngineStatus getStatus() {
        return !_globalStreaming && _status == EngineStatus.STREAMING ? EngineStatus.PAUSED : _status;
    }
}
