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
public interface IUserInterface<DeviceT extends IDevice, SensorT extends ISensor<DeviceT>, TimeT, ValueT> {


    // ITEMS ENUMERATION control part

    public void addDevice(DeviceT device);

    public Enumeration<DeviceT> getDevices();

    public void addOutput(IOutput<TimeT, ValueT> output);

    public Enumeration<IOutput<TimeT, ValueT>> getOutputs();

    public void addLink(SensorT fromSensor, IOutput<TimeT, ValueT> toOutput);


    // ITEMS OPERATION control part

    // WAS it is not safe that the user do this as the current setup provides a static configuration
//    public void initialize(DeviceT device);
//
//    public void finalize(DeviceT device);
//
//    public void initialize(IOutput<TimeT, ValueT> output);
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

    public void setOnStateChanged(EventCallback<IUserInterface<DeviceT, SensorT, TimeT, ValueT>, EngineStatus> callback);

    public void setOnDeviceStateChanged(EventCallback<DeviceT, DeviceStatus> callback);

    public void setOnOutputStateChanged(EventCallback<IOutput<TimeT, ValueT>, OutputStatus> callback);

    public EngineStatus getStatus();
}
