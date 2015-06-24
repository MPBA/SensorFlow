package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.empatica;

import java.util.Arrays;
import java.util.List;

import eu.fbk.mpba.sensorsflows.SensorComponent;

public abstract class EmpaticaSensor extends SensorComponent<Long, double[]> {

    public EmpaticaSensor(EmpaticaDevice p) {
        super(p);
        name = p.getAddress().replace(":", "_");
    }

    private String name;
    private boolean _enabled = true;

    public boolean isOn() {
        return _enabled;
    }

    @Override
    public void switchOnAsync() {
        _enabled = true;
    }

    @Override
    public void switchOffAsync() {
        _enabled = false;
    }


    @Override
    public String toString() {
        return name + "/" + getClass().getSimpleName();
    }

    public static class Battery extends EmpaticaSensor {
        public Battery(EmpaticaDevice p) {
            super(p);
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object) "Battery");
        }
    }

    public static class Accelerometer extends EmpaticaSensor {
        public Accelerometer(EmpaticaDevice p) {
            super(p);
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object) "accX", "accY", "accZ");
        }
    }

    public static class IBI extends EmpaticaSensor {
        public IBI(EmpaticaDevice p) {
            super(p);
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object) "IBI");
        }
    }

    public static class Termometer extends EmpaticaSensor {
        public Termometer(EmpaticaDevice p) {
            super(p);
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object) "Temperature");
        }
    }

    public static class GSR extends EmpaticaSensor {
        public GSR(EmpaticaDevice p) {
            super(p);
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object) "GSR");
        }
    }

    public static class BVP extends EmpaticaSensor {
        public BVP(EmpaticaDevice p) {
            super(p);
        }

        @Override
        public List<Object> getValuesDescriptors() {
            return Arrays.asList((Object) "BVP");
        }
    }
}
