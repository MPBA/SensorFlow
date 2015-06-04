package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import android.content.Context;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;

public class CSVLoaderDevice implements DevicePlugin<Long, double[]>, IMonotonicTimestampReference
{
    private List<SensorComponent<Long, double[]>> _sensors;
    private String name;

    //Accetta la lista di ( streamIN, (separatoreCampo, separatoreColonna) )
    public CSVLoaderDevice(Context context, String name)
    {
        this.name = name;
        _sensors = new ArrayList<>();
        //Un solo file per sensore!
        //TODO Dee) il file me lo danno in input attraverso un inputStreamReader

    }

    /**
     * @param is stream di input
     * @param fieldSeparator separatore dei vari campi
     * @param rowSeparator separatore delle varie righe di campi.
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n"
     */
    void addFile(InputStreamReader is, String fieldSeparator, String rowSeparator) throws IOException
    {
        /*_sensors.add(new CSVLoaderSensor(";", "nomeFILEhahaNONsoBOHperche'COSI'vabe'COSAfaiVIENIqua'MAperche'CHIloSA'qualunqueCOSAfaiSIAMOsempreNEIguai", this));*/
    }
    /**
     * @param is stream di input
     * @param fieldSeparator separatore dei vari campi
     *
     * Separatore di riga di default: "\n"
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n"
     */
    void addFile(InputStreamReader is, String fieldSeparator) throws IOException {addFile(is, fieldSeparator, "\n");}
    /**
     * @param is stream di input
     *
     * Separatore di campo di default: ";"
     * Separatore di riga di default: "\n"
     *
     * AVVERTENZA: ricordati che "\n" e' diverso da "\r\n"
     */
    void addFile(InputStreamReader is) throws IOException {addFile(is, ";", "\n");}


    /**
     * Qui faccio partire i sensori!
     * Chiamato automaticamente da quanto ne so
     */
    @Override public void inputPluginInitialize()
    {
        _sensors.get(0).switchOnAsync();
    }

    @Override public void inputPluginFinalize()
    {
        // Finalizzo
    }

    @Override public Iterable<SensorComponent<Long, double[]>> getSensors()
    {
        return _sensors;
    }

    //Tutto per il timestamp, non metodi miei quindi non saprei documentarli
    private long refUTCNanos;
    public void resetMonoTimestamp(long timestampMillis, long realTimeNanos) {refUTCNanos = timestampMillis * 1000000 - realTimeNanos;}
    public long getMonoTimestampNanos(long realTimeNanos) {return realTimeNanos + refUTCNanos;}

}
