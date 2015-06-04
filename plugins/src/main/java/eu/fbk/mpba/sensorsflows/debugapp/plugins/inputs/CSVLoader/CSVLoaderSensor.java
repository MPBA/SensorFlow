package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import android.hardware.SensorEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;


/**
 * GESTIONE ERRORI: Se dovesse esserci qualsivoglia errore lo stato del sensore verra' settato a ERROR
 * E verra' inviato un evento di errore con il testo dell'eccezione.
 */
public class CSVLoaderSensor extends SensorComponent<Long, double[]>
{
    class ValidDouble
    {
        boolean valid;
        double value;
    }

    boolean isrunning;




    boolean tryParseDouble(String value)
    {
        try{Double.parseDouble(value);return true;}catch(NumberFormatException nfe){return false;}
    }

    //TODO Dee) se trovo una o piu' colonne non valide le ignoro o esco fuori un errore e mi chiudo?
    ValidDouble[] EstraiDatiDaStringa(String s)
    {
        String parts[] = {""};//s.split(separator);
        ValidDouble vd[] = new ValidDouble[parts.length];

        for(int i = 0; i < parts.length; i++)
        {
            vd[i]=new ValidDouble();

            if(vd[i].valid = tryParseDouble(parts[i]))
                vd[i].value = Double.parseDouble(parts[i]);
            else
                vd[i].value = Double.NaN;
        }

        return vd;
    }

    //Thread di caricamento
    private Thread thr = new Thread(new Runnable()
    {
        @Override public void run()
        {
            /*changeStatus(SensorStatus.ON);

            String s;
            try
            {
                while((s = br.readLine()) != null)
                {
                    ValidDouble vd[] = EstraiDatiDaStringa(s);

                    //TODO Dee) per adesso se non sono double li setto a 0
                    double valori[] = new double[vd.length];
                    for(int i = 1; i < vd.length; i++)
                    {
                        if(vd[i].valid)
                            valori[i] = vd[i].value;
                        else
                            valori[i] = 0;
                    }

                    //TODO Dee) QUALE e' il timestamp????? il primo?
                    sensorValue((long)vd[0].value, valori);

                    //DEBUG Dee stampo cosa leggo dal file
                    System.out.println(s);
                }
                fr.close();
            }
            catch (IOException e)
            {
                changeStatus(SensorStatus.ERROR);
                sensorEvent(((IMonotonicTimestampReference)getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()), 0, "IOException: " + e.getMessage());
            }*/
        }
    });




    /**
     * @param isr
     * @param fieldSeparator
     * @param rowSeparator
     * @param d
     * @throws IOException
     *
     * ASSUNZIONI: assumo che il csv contiene un'intestazione
     */
    public CSVLoaderSensor(InputStreamReader isr, String fieldSeparator, String rowSeparator, DevicePlugin<Long, double[]> d) throws IOException
    {
        super(d);

        isrunning = false;




    }


    @Override public void switchOnAsync()
    {
        //Faccio partire il thread!!!! (la prima volta!! lo richiamo dal mio device all'inizialization)
        if(getState() != SensorStatus.ERROR && !isrunning)
        {
            thr.start();
            isrunning = true;
        }
    }

    @Override public void switchOffAsync()
    {
        //Jajajajaja dovrei fermarmi? MAI!
    }

    @Override public List<Object> getValuesDescriptors()
    {
        /*if(descriptors == null)
            return null;
        return Arrays.asList(descriptors);*/
        return null;
    }

    @Override public String toString()
    {
        return "CSVLoader sensor";
    }
}
