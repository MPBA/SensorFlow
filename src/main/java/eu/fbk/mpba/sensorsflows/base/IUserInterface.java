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

    void addInput(DeviceT device);

    DeviceT getInput(String name);

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

    void setOnStatusChanged(EventCallback<IUserInterface<DeviceT, SensorT, OutputT>, EngineStatus> callback);

    void setOnDeviceStatusChanged(EventCallback<DeviceT, DeviceStatus> callback);

    void setOnOutputStatusChanged(EventCallback<OutputT, OutputStatus> callback);

    EngineStatus getStatus();
}
