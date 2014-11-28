package eu.fbk.mpba.sensorsflows;

import android.util.Log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
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

    // Callbacks

    @Override public void deviceStateChanged(IDevice device, DeviceStatus state) {
        if (state == DeviceStatus.CONNECTED) {
            synchronized (_devicesToConnect) {
                _devicesToConnect.remove(device);
                if (_status == EngineStatus.PREPARING && _devicesToConnect.isEmpty())
                    setState(EngineStatus.READY);
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


    // Engine

    final String LOG_TAG = "ALE SFW";

    protected  EngineStatus _status = EngineStatus.STANDBY;
    protected  boolean _globalStreaming = false;

    protected Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>> _linksMap = new Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>>();
    protected Hashtable<ISensor, Boolean> _sensorValves = new Hashtable<ISensor, Boolean>();

    protected Set<IDevice> _devices = new HashSet<IDevice>();
    protected Set<IOutput<TimeT, ValueT>> _outputs = new HashSet<IOutput<TimeT, ValueT>>();

    protected Set<ILink<ISensor, IOutput<TimeT, ValueT>>> _userLinks = new HashSet<ILink<ISensor, IOutput<TimeT, ValueT>>>();

    protected final Set<IDevice> _devicesToConnect = new HashSet<IDevice>();

    public FlowsMan() {
        setState(EngineStatus.STANDBY);
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

    @Override public void connect(IDevice device) {
        // The connection state is checked before the start.
        if (_devices.contains(device) &&  device.getState() == DeviceStatus.NOT_CONNECTED) {
            Log.v(LOG_TAG, "Connecting " + device.toString());
            device.connect();
            // TODO 3 Add async end-user end!
        }
        else {
            Log.v(LOG_TAG, "Refused to connect " + device.toString());
        }
    }

    @Override public void close(IDevice device) {
        // The connection state is not checked
        if (_devices.contains(device) &&  device.getState() == DeviceStatus.CONNECTED) {
            Log.v(LOG_TAG, "Closing " + device.toString());
            device.close();
            // TODO 3 Add async end-user end!
        }
        else {
            Log.v(LOG_TAG, "Refused to close " + device.toString());
        }
    }

    @Override public void switchOn(ISensor sensor) {
        // Note the difference with the set streaming
        if (_devices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Starting async " + sensor.toString());
            sensor.switchOnAsync();
            // TODO 3 Add async end-user end!
        }
        else {
            Log.v(LOG_TAG, "Refused start async " + sensor.toString());
        }
    }

    @Override public void switchOff(ISensor sensor) {
        // Note the difference with the set streaming
        if (_devices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Stopping async " + sensor.toString());
            sensor.switchOffAsync();
            // TODO 3 Add async end-user end!
        }
        else {
            Log.v(LOG_TAG, "Refused start async " + sensor.toString());
        }
    }

    @Override public void setStreaming(ILink<ISensor, IOutput<TimeT, ValueT>> link, boolean streaming) {
        // TODO 2 Do this also on the ISensors (change the hash table type to Booleaned<ISensor>)
        if (_userLinks.contains(link)) {
            Log.v(LOG_TAG, "Setting streaming " + (streaming ? "ON " : "OFF ") + link.toString());
            link.setEnabled(streaming);
        }
        else {
            // TODO 3 Maybe an exception
            Log.v(LOG_TAG, "Refused to connect " + link.toString());
        }
    }

    @Override public boolean getStreaming(ILink<ISensor, IOutput<TimeT, ValueT>> link) {
        // TODO 2 Do this also on the ISensors (change the hash table type to Booleaned<ISensor>)
        if (_userLinks.contains(link)) {
            return link.getEnabled();
        }
        else {
            Log.v(LOG_TAG, "Link not contained " + link.toString());
            // TODO 3 Maybe an exception
            return false;
        }
    }

    @Override public void start() {
        for (IDevice d : _devices) {
            if (d.getState() == DeviceStatus.CONNECTING) {
                throw new UnsupportedOperationException(
                        "One or more devices are still CONNECTING. Please wait that every device is connected or disconnected before calling this method.");
            }
        }
        setState(EngineStatus.PREPARING);
        renderTheMap();
        for (IDevice d : _devices) {
            // if NOT_CONNECTED: checked in the connect method
            connect(d);
        }
    }

    protected void renderTheMap() {
        // Render the links map
        // Build inputs
        //     put an entry for each sensor (for each device)
        for (IDevice d : _devices) {
            for (ISensor s : d.getSensors()){
                _linksMap.put(s, new ArrayList<Booleaned<IOutput<TimeT, ValueT>>>());
                _sensorValves.put(s, false);
            }
        }
        // Set outputs
        //     put an output in the corresponding input's entry for each link specified
        for (ILink<ISensor, IOutput<TimeT, ValueT>> l : _userLinks) {
            _linksMap.get(l.getInput()).add(new Booleaned<IOutput<TimeT, ValueT>>(l.getOutput(), l.getEnabled()));
        }
    }

    @Override public void setStreaming(boolean status) {
        _globalStreaming = status;
    }

    @Override public boolean getStreaming() {
        return _globalStreaming;
    }

    // TODO 4 Check if it is problematic to put this constraint between the connection with the devices and the instance's lifetime.
    @Override public void close() {
        for (IDevice d : _devices) {
            // if CONNECTED: checked in the method
            close(d);
        }
    }

    EngineStatusCallback<IUserInterface<TimeT, ValueT>> _onStateChanged = null;

    public void setState(EngineStatus status) {
        _status = status;
        if (_onStateChanged != null)
            _onStateChanged.handle(this, _status);
    }

    @Override public void setOnStateChanged(EngineStatusCallback<IUserInterface<TimeT, ValueT>> callback) {
        _onStateChanged = callback;
    }

    @Override public EngineStatus getStatus() {
        return _status;
    }
}
