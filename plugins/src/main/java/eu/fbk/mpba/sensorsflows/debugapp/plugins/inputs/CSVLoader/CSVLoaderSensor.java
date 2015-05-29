package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;

public class CSVLoaderSensor extends SensorComponent<Long, double[]>
{


    public CSVLoaderSensor(DevicePlugin<Long, double[]> d) throws FileNotFoundException {
        super(d);

        /*FileReader fr = null;
        fr = new FileReader("FileReaderDemo.java");
        BufferedReader br = new BufferedReader(fr);
        String s;
        while((s = br.readLine()) != null) {
            System.out.println(s);
        }
        fr.close();*/

    }


    @Override public void switchOnAsync()
    {//TODO far partire il thread!!!! (la prima volta!! lo richiamo dal mio device all'inizialization
    }

    @Override public void switchOffAsync()
    {

    }

    @Override public List<Object> getValuesDescriptors()
    {
        return null;
    }

    @Override public String toString()
    {
        return null;
    }
}
