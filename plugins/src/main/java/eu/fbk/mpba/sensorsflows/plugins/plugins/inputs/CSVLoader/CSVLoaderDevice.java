package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.CSVLoader;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;



/**
 * [IMPORTANTE] Vai a vedere il javadoc di CSVLoaderSensor,
 * non lo riporto qui per non creare ridondanza ovviamente.
*/
public class CSVLoaderDevice implements DevicePlugin<Long, double[]>, IMonotonicTimestampReference
{
    protected List<SensorComponent<Long, double[]>> _sensors;
    protected String _name;
    protected Runnable onfinish = null;

    /**
     * @param name nome del Device
     *     I vari file vanno aggiunti con addFile.
     */
    public CSVLoaderDevice(String name)
    {
        _name = name;
        _sensors = new LinkedList<>();
    }

    /**
     * Thread con cui i sensori a turno caricano i loro dati.
     */
    private Thread thr = new Thread(new Runnable(){@Override public void run()
    {
        for(SensorComponent s : _sensors)
            s.switchOnAsync();

        boolean ceAncoraSperanza = false;
        do
        {
            for(SensorComponent s:_sensors)
                ceAncoraSperanza = ((CSVLoaderSensor)s).sendRow();
        } while(ceAncoraSperanza);

        if(onfinish != null)
            onfinish.run();
    }});

    /**
     * @param is stream di input
     * @param fieldSeparator separatore dei vari campi
     * @param rowSeparator separatore delle varie righe di campi.
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n" ovviamente.
     */
    public void addFile(InputStreamReader is, String fieldSeparator, String rowSeparator, long tsScale, String name) throws Exception {
        _sensors.add(new CSVLoaderSensor(is, fieldSeparator, rowSeparator, tsScale, name, this));
    }
    public void addFile(InputStreamReader is, String fieldSeparator, String rowSeparator, long tsScale) throws Exception {addFile(is, fieldSeparator, rowSeparator, tsScale, "");}
    public void addFile(InputStreamReader is, String fieldSeparator, String rowSeparator, String name) throws Exception {addFile(is,fieldSeparator,rowSeparator,1,name);}
    public void addFile(InputStreamReader is, String fieldSeparator, String rowSeparator) throws Exception {addFile(is,fieldSeparator,rowSeparator,1,"");}


    public void setAsyncActionOnFinish(Runnable action)
    {
        onfinish = action;
    }

    /**
     * Qui faccio partire i sensori
     * Chiamato almeno una volta
     */
    @Override public void inputPluginInitialize() {
        //Devo far partire un thread per non bloccare il tutto.
        thr.start();
    }
    @Override public void inputPluginFinalize() {
        // Finalizzo
    }
    @Override public Iterable<SensorComponent<Long, double[]>> getSensors()
    {
        return _sensors;
    }
    @Override public String toString() {
        return _name;
    }


    //Questi metodi sono per il timestamp, non sono metodi miei quindi non saprei documentarli
    private long refUTCNanos;
    public void resetMonoTimestamp(long timestampMillis, long realTimeNanos) {refUTCNanos = timestampMillis * 1000000 - realTimeNanos;}
    public long getMonoTimestampNanos(long realTimeNanos) {return realTimeNanos + refUTCNanos;}
}
