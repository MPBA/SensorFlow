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
public class CSVLoaderSensor extends SensorComponent<Long, double[]> {
    CSVHandler ch;
    protected String name;
    static int globalDebugID = 0;
    protected boolean fileFinito = false;

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
            sensorEvent(((CSVLoaderDevice) getParentDevicePlugin()).getMonoUTCNanos(System.nanoTime()), 101, "[ " + name + " ] Error reading row: " + e.getMessage());
            mStatus = SensorStatus.ERROR;
            fileFinito = true;
        }

        if (r != null) {
            if (!r.getError())
                sensorValue(r.timestamp, r.fields);
            else {
                sensorEvent(r.timestamp, 101, r.getErrorMsg());
                mStatus = SensorStatus.ERROR;
            }

            if (r.endfile)
                fileFinito = true;
        }

        return !fileFinito;
    }

    //Per poter fare i test estendendo questa classe
    public void sensorValue(long time, double[] value) {
        super.sensorValue(time, value);
    }

    public void sensorEvent(long time, int type, String message) {
        super.sensorEvent(time, type, message);
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
    public List<Object> getValuesDescriptors() {
        return ch.getDescriptors();
    }

    @Override
    public String getName() {
        return getParentDevicePlugin().getName() + "/" + name;
    }
}
