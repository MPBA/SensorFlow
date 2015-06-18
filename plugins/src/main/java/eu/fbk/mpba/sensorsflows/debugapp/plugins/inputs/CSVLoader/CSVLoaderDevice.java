package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;



/**
 * [IMPORTANTE] Vai a vedere il javadoc di CSVLoaderSensor,
 * non lo riporto qui per non creare ridondanza ovviamente.
*/
public class CSVLoaderDevice implements DevicePlugin<Long, double[]>, IMonotonicTimestampReference
{
    private List<SensorComponent<Long, double[]>> _sensors;
    private String _name;

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
     * @param is stream di input
     * @param fieldSeparator separatore dei vari campi
     * @param rowSeparator separatore delle varie righe di campi.
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n"
     */
    public void addFile(InputStreamReader is, String fieldSeparator, String rowSeparator) throws IOException {
        _sensors.add(new CSVLoaderSensor(is, fieldSeparator, rowSeparator, this));
    }
    /**
     * @param is stream di input
     * @param fieldSeparator separatore dei vari campi
     *
     * Separatore di riga di default: "\n"
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n"
     */
    public void addFile(InputStreamReader is, String fieldSeparator) throws IOException {addFile(is, fieldSeparator, "\n");}
    /**
     * @param is stream di input
     *
     * Separatore di campo di default: ";"
     * Separatore di riga di default: "\n"
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n"
     */
    public void addFile(InputStreamReader is) throws IOException {addFile(is, ";", "\n");}



    /**
     * Qui faccio partire i sensori
     * Chiamato almeno una volta
     */
    @Override public void inputPluginInitialize()
    {
        //Devo far partire un thread per non bloccare il tutto.
        _sensors.forEach(SensorComponent::switchOnAsync);
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
