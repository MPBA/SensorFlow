package eu.fbk.mpba.sensorsflows.stubs;

import java.util.List;

import eu.fbk.mpba.sensorsflows.OutputImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.base.SensorDataEntry;
import eu.fbk.mpba.sensorsflows.base.SensorEventEntry;

/**
 * Classe di esempio.
 * Estende la classe SensorImpl che è astratta ed è parte della libreria.
 *
 * Un Sensore rappresenta una sorgente di dati simultanei ovvero di coppie tempo-insieme di valori
 * (ad esempio accelerometro &lt;time, (ax,ay,az)&gt; e giroscopio &lt;time, (gx,gy,gz)&gt; sono due
 * sensori diversi.
 *
 * Si presuppone che in questa classe vengano implementati dei metodi callback richiamati dal thread
 * del sensore reale che poi notifichino tramite i metodi
 *      public void sensorValue(TimeT time, ValueT value)
 * e
 *      public void sensorEvent(TimeT time, int type, String message)
 * protetti quindi visibili nella sottoclasse.
 */
public class OutputStub extends OutputImpl<Long, float[]> {

    /**
     * Costruttore pienamente personalizzato
     * Io ho aggiunto un nome per debug che viene messo in toString.
     */
    public OutputStub(String name) {
        this.name = name;
    }

    String name;

    /**
     * Qua va inizializzato tutto quello che non ha abbastanza precedenza da essere inizializzato
     * prima dell'avvio della libreria. Meglio se le connessioni sono gestite prima dello start.
     */
    @Override
    protected void pluginInitialize() {

    }

    /**
     * Qualsiasi cosa da eseguire quando la libreria viene chiusa.
     */
    @Override
    protected void pluginFinalize() {

    }

    /**
     * La libreria rende disponibile un evento da uno specifico sensore.
     */
    @Override
    protected void newSensorEvent(SensorEventEntry event) {

    }

    /**
     * La libreria rende disponibile un dato da uno specifico sensore.
     */
    @Override
    protected void newSensorData(SensorDataEntry<Long, float[]> data) {

    }

    /**
     * Vengono notificati i sensori associati (ad esempio per inizializzare files o tabelle).
     * @param linkedSensors lista di sensori
     */
    @Override
    public void setLinkedSensors(List<SensorImpl> linkedSensors) {

    }

    @Override
    public String toString() {
        return "OutputStub:" + name;
    }
}
