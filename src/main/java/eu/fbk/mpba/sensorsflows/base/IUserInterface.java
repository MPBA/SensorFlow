package eu.fbk.mpba.sensorsflows.base;

import eu.fbk.mpba.sensorsflows.Output;

/**
 * Main interface for the flows control.
 *
 * The instance is used by the user!
 *
 * The user can control the enumeration of the devices and the outputs and the links between
 * these, their operation and the operation of the engine.
 */
@SuppressWarnings("UnusedDeclaration")
public interface IUserInterface<DeviceT, SensorT extends ISensor, OutputT extends Output> {


    // ITEMS ENUMERATION control part

    IUserInterface<DeviceT, SensorT, OutputT> addInput(DeviceT device);

    DeviceT getInput(String name);

    Iterable<DeviceT> getDevices();

    IUserInterface<DeviceT, SensorT, OutputT> addOutput(OutputT output);

    OutputT getOutput(String name);

    Iterable<OutputT> getOutputs();

    IUserInterface<DeviceT, SensorT, OutputT> addLink(SensorT fromSensor, OutputT toOutput);

    // ITEMS OPERATION control part

    IUserInterface<DeviceT, SensorT, OutputT> setOutputEnabled(boolean enabled, String name);

    boolean getOutputEnabled(String name);

    // ENGINE OPERATION control part

    IUserInterface<DeviceT, SensorT, OutputT> start();

    IUserInterface<DeviceT, SensorT, OutputT> setPaused(boolean streaming);

    boolean isPaused();

    IUserInterface<DeviceT, SensorT, OutputT> stop();

    void close();

    IUserInterface<DeviceT, SensorT, OutputT> setOnStatusChanged(EventCallback<IUserInterface<DeviceT, SensorT, OutputT>, EngineStatus> callback);

    IUserInterface<DeviceT, SensorT, OutputT> setOnDeviceStatusChanged(EventCallback<DeviceT, DeviceStatus> callback);

    IUserInterface<DeviceT, SensorT, OutputT> setOnOutputStatusChanged(EventCallback<OutputT, OutputStatus> callback);

    EngineStatus getStatus();
}
