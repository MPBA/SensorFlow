package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class CSVLoaderDevice implements DevicePlugin<Long, double[]>
{
    //TODO tengo una lista di sensori
    private List<SensorComponent<Long, double[]>> _sensors;
    private String name;

    public CSVLoaderDevice(Context context, String name) {
        this.name = name;
        _sensors = new ArrayList<>();
        //_sensors.add(new CSVLoaderSensor(this));
    }

    @Override public void inputPluginInitialize()
    {
        //TODO Inizializzo il tutto (chiamato quando schiacciaidfod il bottone NO BLOCKING
    }

    @Override public void inputPluginFinalize()
    {
        //TODO Finalizzo
    }

    @Override public Iterable<SensorComponent<Long, double[]>> getSensors()
    {
        //TODO RItorno la lista?
        return null;
    }
}
