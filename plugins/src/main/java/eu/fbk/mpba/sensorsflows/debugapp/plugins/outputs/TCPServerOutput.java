package eu.fbk.mpba.sensorsflows.debugapp.plugins.outputs;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputPlugin;
import eu.fbk.mpba.sensorsflows.base.ISensor;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

public class TCPServerOutput implements OutputPlugin<Long, double[]> {

    protected Socket mSock;

    public TCPServerOutput(InetAddress local, int port) {
        mSock = new Socket();
    }

    @Override
    public void outputPluginInitialize(Object sessionTag, List<ISensor> streamingSensors) {

    }

    @Override
    public void outputPluginFinalize() {

    }

    @Override
    public void newSensorEvent(SensorEventEntry<Long> event) {

    }

    @Override
    public void newSensorData(SensorDataEntry<Long, double[]> data) {

    }
}
