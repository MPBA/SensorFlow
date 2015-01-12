package eu.fbk.mpba.sensorsflows.stubs;

import android.content.Context;

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
public class SmartphoneDevice extends DeviceImpl<Long, float[]>  {

    private String _name;
    private List<SensorImpl<Long, float[]>> _sensors;

    /**
     * Costruttore pienamente personalizzato
     *
     * Ho messo un nome per il toString e la lista dei sensori
     * I sensori vanno creati qua
     */
    public SmartphoneDevice(Context c) {
        _name = "Smartphone";
        _sensors = new ArrayList<SensorImpl<Long, float[]>>(_sensors);

        AccelerometerSensor a = new AccelerometerSensor(this, c);
        _sensors.add(a);
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
        for (SensorImpl<Long, float[]> s : _sensors) {
            s.switchOnAsync();
        }
    }

    /**
     * Qualsiasi cosa da eseguire quando la libreria viene chiusa.
     */
    @Override
    protected void pluginFinalize() {

    }

    @Override
    public String toString() {
        return "Device:" + _name;
    }

}
