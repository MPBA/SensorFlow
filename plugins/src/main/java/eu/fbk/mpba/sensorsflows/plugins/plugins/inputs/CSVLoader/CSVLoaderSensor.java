package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.CSVLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

/**
 * ASSUNZIONI: assumo che...
 *                  - Abbiate letto le assunzioni di CSVHandler
 *
 * GESTIONE ERRORI: - Se dovesse esserci un qualsivoglia errore nel costruttore verra' lanciata un'eccezione, altrimenti
 *                      lo stato del sensore verra' settato ad 'ERROR' e verra' inviato un evento Error con il testo dell'eccezione/errore.
 */
public class CSVLoaderSensor extends SensorComponent<Long, double[]>
{
    CSVHandler ch;
    static int globalDebugID = 0;
    protected int debugID = 0;
    protected boolean fileFinito = false;

    public CSVLoaderSensor(InputStreamReader isr, String fieldSeparator, String rowSeparator, DevicePlugin<Long, double[]> d) throws Exception {
        this(isr, fieldSeparator, rowSeparator, 1, d);
    }
    public CSVLoaderSensor(InputStreamReader isr, String fieldSeparator, String rowSeparator, long tsScale, DevicePlugin<Long, double[]> d) throws Exception {
        super(d);
        debugID = globalDebugID++;
        ch = new CSVHandler(isr,fieldSeparator, rowSeparator, tsScale);
        _status = SensorStatus.ON;
    }

    /**
     * @return true se devo ancora leggere, false se ho finito oppure c'e' stato un errore.
     */
    public boolean sendRow()
    {
        if(fileFinito)
            return false;

        CSVHandler.CSVRow r = null;
        try { r = ch.getNextRow(); } catch (IOException e) {
            sensorEvent(((CSVLoaderDevice) getParentDevicePlugin()).getMonoTimestampNanos(System.nanoTime()), 101, "[SID"+ debugID + "] Error reading row: " + e.getMessage());
            _status = SensorStatus.ERROR;
            fileFinito = true;
        }

        if(r!= null)
        {
            if(!r.getError())
                sensorValue(r.timestamp, r.fields);
            else
            {
                sensorEvent(r.timestamp, 101, r.getErrorMsg());
                _status = SensorStatus.ERROR;
            }

            if(r.endfile)
                fileFinito = true;
        }

        return !fileFinito;
    }

    public void sensorValue(long time, double[] value) {
        super.sensorValue(time, value);
    }

    public void sensorEvent(long time, int type, String message) {
        super.sensorEvent(time, type, message);
    }

    @Override public void switchOnAsync() {
        //Boh qui devo far qualcosa?
    }
    @Override public void switchOffAsync() {
        //Jajajajaja dovrei fermarmi? MAI!
    }

    @Override public List<Object> getValuesDescriptors() {
        return ch.getDescriptors();
    }
    @Override public String toString() {
        return "CSVLoader sensor" + debugID;
    }
}
