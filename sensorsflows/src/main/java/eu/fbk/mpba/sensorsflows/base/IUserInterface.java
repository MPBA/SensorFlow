package eu.fbk.mpba.sensorsflows.base;

import eu.fbk.mpba.sensorsflows.OutputPlugin;

/**
 * Main interface for the flows control.
 *
 * The instance is used by the user!
 *
 * The user can control the enumeration of the devices and the outputs and the links between
 * these, their operation and the operation of the engine.
 */
@SuppressWarnings("UnusedDeclaration")
public interface IUserInterface<DeviceT, SensorT extends ISensor, OutputT extends OutputPlugin> {


    // ITEMS ENUMERATION control part

    public void addDevice(DeviceT device);

    public Iterable<DeviceT> getDevices();

    public void addOutput(OutputT output);

    public Iterable<OutputT> getOutputs();

    public void addLink(SensorT fromSensor, OutputT toOutput);


    // ITEMS OPERATION control part

// WAS it is not safe that the user does this as the current setup provides a static configuration
//    public void initializeDevice(DeviceT device);
//
//    public void finalize(DeviceT device);
//
//    public void initializeDevice(IOutput<TimeT, ValueT> output);
//
//    public void finalize(IOutput<TimeT, ValueT> output);

    public void switchOn(SensorT sensor);

    public void switchOff(SensorT sensor);

    public boolean isSensorListened(SensorT sensor);

    public void setSensorListened(SensorT sensor, boolean streaming);


    // ENGINE OPERATION control part

    public void start();

    public void setPaused(boolean streaming);

    public boolean isPaused();

    public void close();

    public void setOnStateChanged(EventCallback<IUserInterface<DeviceT, SensorT, OutputT>, EngineStatus> callback);

    public void setOnDeviceStateChanged(EventCallback<DeviceT, DeviceStatus> callback);

    public void setOnOutputStateChanged(EventCallback<OutputT, OutputStatus> callback);

    public EngineStatus getStatus();
}
