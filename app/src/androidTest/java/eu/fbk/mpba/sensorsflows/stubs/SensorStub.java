package eu.fbk.mpba.sensorsflows.stubs;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.base.SensorStatus;

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
public class SensorStub extends SensorImpl<Long, float[]> {

    /**
     * Costruttore pienamente personalizzato
     * Io ho aggiunto un nome per debug che viene messo in toString.
     *
     * VA impostato il parent device
     *
     * Il prima possibile va impostato il parent device del sensore con setParentDevice(...)
     */
    public SensorStub(String name, DeviceStub parent) {
        this.name = name;
        this.setParentDevice(parent);
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
     * Va gestita una variabile d'istanza di tipo {@code SensorStatus}, questo è il getter.
     * @return stato
     */
    @Override
    public SensorStatus getState() {
        return null;
    }

    @Override
    public String toString() {
        return "SensorStub-" + name;
    }

    @Override
    public List<Object> getValuesDescriptors() {
        return Arrays.asList((Object)"Random");
    }
}
