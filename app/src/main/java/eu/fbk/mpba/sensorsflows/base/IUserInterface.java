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

    public void addLink(ILink<ISensor, IOutput<TimeT, ValueT>> link);

    public Enumeration<ILink<ISensor, IOutput<TimeT, ValueT>>> getLinks();


    // ITEMS OPERATION control part

    public void connect(IDevice device);

    public void switchOn(ISensor sensor);

    public void setStreaming(ILink<ISensor, IOutput<TimeT, ValueT>> link, boolean streaming);

    public boolean getStreaming(ILink<ISensor, IOutput<TimeT, ValueT>> link);

    public void switchOff(ISensor sensor);

    public void close(IDevice device);


    // ENGINE OPERATION control part

    public void start();

    public void setStreaming(boolean streaming);

    public boolean getStreaming();

    public void close();

    public void setOnStateChanged(EngineStatusCallback<IUserInterface<TimeT, ValueT>> callback);

    public EngineStatus getStatus();
}
