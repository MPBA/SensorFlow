package eu.fbk.mpba.sensorsflows.stubs;

import java.util.ArrayList;
import java.util.List;

import eu.fbk.mpba.sensorsflows.DeviceImpl;
import eu.fbk.mpba.sensorsflows.SensorImpl;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

/**
 * Classe di esempio.
 * Estende la classe DeviceImpl che è astratta ed è parte della libreria.
 *
 * Un Device rappresenta un insieme di sensori accomunati da un dispositivo che si connette
 * all'inizio una volta per tutti i sensori (es. E3 è il Device che racchiude i sensori BVP Temp
 * ecc. oppure Smartphone può essere il Device che comprende acc. gyro. & co.)
 */
public class DeviceStub extends DeviceImpl<Long, float[]> {

    private String _name;
    private List<SensorImpl<Long, float[]>> _sensors;

    /**
     * Costruttore pienamente personalizzato
     * Io ho aggiunto un nome per debug che viene messo in toString e un numero di sensori fittizi
     * da generare.
     * I sensori vanno creati qua
     */
    public DeviceStub(String name, int sensors) {

        _name = name;
        _sensors = new ArrayList<SensorImpl<Long, float[]>>(sensors);
        for (int i = 0; i < sensors; i++) {
            this._sensors.add(new SensorStub(name + "-" + i, this));
        }
    }

    /**
     * Ritorna un iretabile read only dei sensori.
     * La sintassi è un po' verbose ma intanto va così.
     * @return iterable
     */
    @Override
    public Iterable<SensorImpl<Long, float[]>> getSensors() {
        return new ReadOnlyIterable<SensorImpl<Long, float[]>>(_sensors.iterator());
    }

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

    @Override
    public String toString() {
        return "DeviceStub:" + _name;
    }
}
