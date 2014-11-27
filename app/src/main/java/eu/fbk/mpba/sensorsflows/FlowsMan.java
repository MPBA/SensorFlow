package eu.fbk.mpba.sensorsflows;

import android.util.Log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import eu.fbk.mpba.sensorsflows.base.Booleaned;
import eu.fbk.mpba.sensorsflows.base.DeviceStatus;
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
    // TODO 4 Implement callbacks
    @Override public void deviceEvent(IDevice device, int type, String message) {

    }

    @Override public void deviceStateChanged(IDevice device, DeviceStatus state) {

    }

    @Override public void pushData(ISensor sensor, TimeT time, ValueT value) {

    }

    @Override public void sensorEvent(ISensor sensor, int type, String message) {

    }

    @Override public void sensorStateChanged(ISensor sensor, SensorStatus state) {

    }


    // Engine

    final String LOG_TAG = "ALE SFW";

    private boolean _rendered = false;
    private boolean _globalStreaming = false;

    Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>> _linksMap = new Hashtable<ISensor, ArrayList<Booleaned<IOutput<TimeT, ValueT>>>>();
    Set<ILink<ISensor, IOutput<TimeT, ValueT>>> _links = new HashSet<ILink<ISensor, IOutput<TimeT, ValueT>>>();
    Set<IDevice> _devices = new HashSet<IDevice>();
    Set<IOutput<TimeT, ValueT>> _outputs = new HashSet<IOutput<TimeT, ValueT>>();

    public FlowsMan() {

    }

    @Override public void addDevice(IDevice device) {
        if (!_rendered)
            _devices.add(device);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    @Override public void addLink(ILink<ISensor, IOutput<TimeT, ValueT>> link) {
        if (!_rendered)
            _links.add(link);
        else
            throw new UnsupportedOperationException("The map is already rendered. No inputs, outputs or links can be added now.");
    }

    @Override public void addOutput(IOutput<TimeT, ValueT> output) {
        if (!_rendered)
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
        return new IterToEnum<ILink<ISensor, IOutput<TimeT, ValueT>>>(_links.iterator());
    }

    @Override public void connect(IDevice device) {
        if (_devices.contains(device) &&  device.getState() == DeviceStatus.DISCONNECTED) {
            Log.v(LOG_TAG, "Connecting " + device.toString());
            device.connect();
        }
        else {
            Log.v(LOG_TAG, "Refused to connect " + device.toString());
        }
    }

    @Override public void close(IDevice device) {
        if (_devices.contains(device) &&  device.getState() == DeviceStatus.CONNECTED) {
            Log.v(LOG_TAG, "Closing " + device.toString());
            device.close();
        }
        else {
            Log.v(LOG_TAG, "Refused to close " + device.toString());
        }
    }

    @Override public void enable(ISensor sensor) {
        if (_devices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Starting async " + sensor.toString());
            sensor.startStreamingAsync();
        }
        else {
            Log.v(LOG_TAG, "Refused start async " + sensor.toString());
        }
    }

    @Override public void disable(ISensor sensor) {
        if (_devices.contains(sensor.getParentDevice())) {
            Log.v(LOG_TAG, "Stopping async " + sensor.toString());
            sensor.stopStreamingAsync();
        }
        else {
            Log.v(LOG_TAG, "Refused start async " + sensor.toString());
        }
    }

    @Override public void setStreaming(ILink<ISensor, IOutput<TimeT, ValueT>> link, boolean streaming) {
        if (_links.contains(link)) {
            Log.v(LOG_TAG, "Setting streaming " + (streaming ? "ON " : "OFF ") + link.toString());
            link.setEnabled(streaming);
        }
        else {
            Log.v(LOG_TAG, "Refused to connect " + link.toString());
        }
    }

    @Override public boolean getStreaming(ILink<ISensor, IOutput<TimeT, ValueT>> link) {
        if (_links.contains(link)) {
            return link.getEnabled();
        }
        else {
            Log.v(LOG_TAG, "Link not contained " + link.toString());
            // TODO 3 Maybe an exception
            return false;
        }
    }

    @Override public void renderAndStart() {
        for (IDevice d : _devices) {
            if (d.getState() == DeviceStatus.DISCONNECTED)
                connect(d);
        }
        renderTheMap();
        _rendered = true;

        // TODO 1 wait for everyone connected
        // TODO 2 Tell to the user that we can start
    }

    private void renderTheMap() {
        // Render the links map
        // Build inputs
        //     put an entry for each sensor (for each device)
        for (IDevice d : _devices) {
            for (ISensor s : d.getSensors()){
                _linksMap.put(s, new ArrayList<Booleaned<IOutput<TimeT, ValueT>>>());
            }
        }
        // Set outputs
        //     put an output in the corresponding input's entry for each link specified
        for (ILink<ISensor, IOutput<TimeT, ValueT>> l : _links) {
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
    @Override public void stopAndClose() {
        for (IDevice d : _devices) {
            close(d);
        }
    }

    // TODO 0 Add a state callback with EngineStatus and accessors
}
