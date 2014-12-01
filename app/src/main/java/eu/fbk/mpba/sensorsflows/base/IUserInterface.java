package eu.fbk.mpba.sensorsflows.base;

import java.util.Enumeration;

/**
 * Main interface for the flows control.
 *
 * The instance is used by the user!
 *
 * The user can control the enumeration of the devices and the outputs and the links between
 * these, their operation and the operation of the engine.
 */
public interface IUserInterface<TimeT, ValueT> {


    // ITEMS ENUMERATION control part

    public void addDevice(IDevice device);

    public Enumeration<IDevice> getDevices();

    public void addOutput(IOutput<TimeT, ValueT> output);

    public Enumeration<IOutput<TimeT, ValueT>> getOutputs();

    public void addLink(ISensor fromSensor, IOutput<TimeT, ValueT> toOutput, boolean initialState);


    // ITEMS OPERATION control part

    public void initialize(IDevice device);

    public void finalize(IDevice device);

    public void initialize(IOutput<TimeT, ValueT> output);

    public void finalize(IOutput<TimeT, ValueT> output);

    public void switchOn(ISensor sensor);

    public void switchOff(ISensor sensor);

    public boolean isSensorListened(ISensor sensor);

    public void setSensorListened(ISensor sensor, boolean streaming);


    // ENGINE OPERATION control part

    public void start();

    public void setPaused(boolean streaming);

    public boolean isPaused();

    public void close();

    public void setOnStateChanged(EventCallback<IUserInterface<TimeT, ValueT>, EngineStatus> callback);

    public void setOnDeviceStateChanged(EventCallback<IDevice, DeviceStatus> callback);

    public void setOnOutputStateChanged(EventCallback<IOutput<TimeT, ValueT>, OutputStatus> callback);

    public EngineStatus getStatus();
}
