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

    void addDevice(DeviceT device);

    DeviceT getDevice(String name);

    Iterable<DeviceT> getDevices();

    void addOutput(OutputT output);

    OutputT getOutput(String name);

    Iterable<OutputT> getOutputs();

    void addLink(SensorT fromSensor, OutputT toOutput);

    // ITEMS OPERATION control part

    void setOutputEnabled(boolean enabled, String name);

    boolean getOutputEnabled(String name);

    // ENGINE OPERATION control part

    void start();

    void setPaused(boolean streaming);

    boolean isPaused();

    void stop();

    void close();

    void setOnStateChanged(EventCallback<IUserInterface<DeviceT, SensorT, OutputT>, EngineStatus> callback);

    void setOnDeviceStateChanged(EventCallback<DeviceT, DeviceStatus> callback);

    void setOnOutputStateChanged(EventCallback<OutputT, OutputStatus> callback);

    EngineStatus getStatus();
}
