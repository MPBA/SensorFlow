package eu.fbk.mpba.sensorsflows.debugapp.plugins.inputs.CSVLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonotonicTimestampReference;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * ASSUNZIONI: assumo che...
 *                  - Il file CSV contenga un'intestazione
 *                  - Nell'intestazione un campo coincida con la stringa [toLower] 'ts' oppure 'timestamp'
 *                  - Tutti i campi siano numerici (righe con campi non validi verranno ignorate e riportate come errore)
 *                  - Abbiate letto le assunzioni di CSVHandler
 *
 * GESTIONE ERRORI: - Se dovesse esserci un qualsivoglia errore nel costruttore verra' lanciata un'eccezione, altrimenti
 *                      lo stato del sensore verra' settato ad 'ERROR' e verra' inviato un evento Error
 *                      con il testo dell'eccezione/errore.
 */
public class CSVLoaderSensor extends SensorComponent<Long, double[]>
{
    CSVHandler ch;
    static int globalDebugID = 0;
    int debugID = 0;

    public CSVLoaderSensor(InputStreamReader isr, String fieldSeparator, String rowSeparator, DevicePlugin<Long, double[]> d) throws Exception {
        super(d);
        debugID = globalDebugID++;
        ch = new CSVHandler(debugID,isr,fieldSeparator, rowSeparator);

    }

    /**
     * @return true se devo ancora leggere, false se ho finito oppure c'e' stato un errore.
     */
    public boolean sendRow() {

        if(getState() == SensorStatus.ERROR)
            return false;

        CSVHandler.CSVRow r = null;
        try { r = ch.getNextRow(); } catch (IOException e) {
            sensorEvent(((CSVLoaderDevice) getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()), 101, "[SID"+ debugID + "] Error reading row: " + e.getMessage());
            _status = SensorStatus.ERROR;
        }

        if(getState() != SensorStatus.ERROR && r == null)
            _status = SensorStatus.OFF;

        if(getState() == SensorStatus.ON)
            sensorValue(r.timestamp, r.fields);

        return getState() == SensorStatus.ON;
    }

    public void sensorValue(long time, double[] value)
    {
        System.out.println("[SID"+ debugID + "] ts:"+time);
    }

    public void sensorEvent(long time, int type, String message)
    {
        System.out.println("[SID"+ debugID + "]"+message);
    }

    @Override public void switchOnAsync()
    {
        //Boh qui devo far qualcosa?
    }

    @Override public void switchOffAsync()
    {
        //Jajajajaja dovrei fermarmi? MAI!
    }

    @Override public List<Object> getValuesDescriptors()
    {
        return ch.getDescriptors();
    }

    @Override public String toString()
    {
        return "CSVLoader sensor";
    }
}
