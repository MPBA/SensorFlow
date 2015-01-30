package eu.fbk.mpba.sensorsflows.gpspluginapp.stubs;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.SensorComponent;

/**
 * Classe di esempio.
 * Estende la classe SensorImpl che è astratta ed è parte della libreria.
 *
 * Un Sensore rappresenta una sorgente di dati simultanei ovvero di coppie tempo - insieme di valori
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
public class SensorStub extends SensorComponent<Long, float[]> {

    /**
     * Costruttore pienamente personalizzato
     * Io ho aggiunto un nome che viene messo in toString.
     *
     * VA impostato il parent device
     *
     * Il prima possibile va impostato il parent device del sensore
     */
    public SensorStub(String name, DevicePlugInStub parent) {
        super(parent);
        this.name = name;
    }

    String name;

    /**
     * Power saving
     * Nel caso il device permetta di abilitare o disabilitare alcuni sensori questi metodi verranno
     * chiamati. Sono asincroni quindi dopo l'esecuzione di questo metodo è sufficiente iniziare a
     * trasmettere dati.
     */
    @Override
    public void switchOnAsync() {

    }


    /**
     * Power saving
     * Nel caso il device permetta di abilitare o disabilitare alcuni sensori questi metodi verranno
     * chiamati. Sono asincroni quindi dopo l'esecuzione di questo metodo è sufficiente smettere di
     * trasmettere dati.
     */
    @Override
    public void switchOffAsync() {

    }

    /**
     * Questo metodo è importante perché può ad esempio essere usato per dare nome ai file CSV o
     * a tabelle. Per non creare ambiguità va aggiunto il {@code getParentDevice().toString()}.
     */
    @Override
    public String toString() {
        return getParentDevice().toString() + "/" + name;
    }

    /**
     * Questo metodo è importante perché da la descrizione dei valori del sensore e viene usato per
     * dare nome alle colonne dei CSV o della tabella relativa al sensore.
     */
    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object)"Random", "Random", "Random");
    }
}
