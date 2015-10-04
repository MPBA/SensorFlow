package eu.fbk.mpba.sensorsflows.plugins.inputs.CSVLoader;

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
 *                  - (new) In caso di replay gli stessi file non verranno inviati ma ogni nome verrà notificato come evento con codice -12
 *             Codici:
 *                  -12     File già caricato
 *                  101     IO error
 *                  102     Parser error
 */
public class CSVLoaderSensor extends SensorComponent<Long, double[]> {
    CSVHandler ch;
    protected String name;
    static int globalDebugID = 0;
    protected boolean fileFinito = false;
    int righeLette = 0;

    public CSVLoaderSensor(InputStreamReader isr, String fieldSeparator, String rowSeparator, long tsScale, String sensorName, DevicePlugin<Long, double[]> d) throws Exception {
        super(d);

        name = sensorName;
        if (name.equals(""))
            name = "Sensor_" + (globalDebugID++);

        ch = new CSVHandler(isr, fieldSeparator, rowSeparator, tsScale);
        mStatus = SensorStatus.ON;
    }

    /**
     * @return true se devo ancora leggere, false se ho finito oppure c'e' stato un errore.
     * Metodo che invia una riga del CSV che sto leggendo.
     */
    public boolean sendRow() {
        if (fileFinito)
            return false;

        CSVHandler.CSVRow r = null;
        try {
            r = ch.getNextRow();
        } catch (IOException e) {
            sensorEvent(((CSVLoaderDevice) getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()), 101, "[" + name + "]\t" + e.getMessage());
            mStatus = SensorStatus.ERROR;
            fileFinito = true;
        }

        if (r != null)
        {
            if (r.getError())
            {
                sensorEvent(r.timestamp, 102, r.getErrorMsg());
                mStatus = SensorStatus.ERROR;
            }
            else if(r.isValid())
                sensorValue(r.timestamp, r.fields);

            if (r.endfile)
                fileFinito = true;
        }

        return !fileFinito;
    }

    //Inutili
    @Override
    public void switchOnAsync() {
        //Boh qui devo far qualcosa?
    }

    @Override
    public void switchOffAsync() {
        //Jajajajaja dovrei fermarmi? MAI!
    }

    //Per la libreria sottostante
    @Override
    public List<Object> getValueDescriptor() {
        return ch.getDescriptors();
    }

    @Override
    public String getName() {
        return name;
    }

    public int getReceivedMessagesCount()
    {
        return getForwardedMessagesCount();
    }
    public int getForwardedMessagesCount()
    {
        return righeLette; //TODO inviare il numero di <QUALCOSA> inviato
    }
}
