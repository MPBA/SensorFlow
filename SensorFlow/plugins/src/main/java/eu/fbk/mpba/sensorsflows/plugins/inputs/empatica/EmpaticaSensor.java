package eu.fbk.mpba.sensorsflows.plugins.inputs.empatica;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.fbk.mpba.sensorsflows.SensorComponent;

public abstract class EmpaticaSensor extends SensorComponent<Long, double[]> {

    public EmpaticaSensor(EmpaticaNode p) {
        super(p);
    }

    private boolean _enabled = true;

    public boolean isEnabled() {
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

    public static class Battery extends EmpaticaSensor {
        public Battery(EmpaticaNode p) {
            super(p);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "Battery");
        }
    }

    public static class Accelerometer extends EmpaticaSensor {
        public Accelerometer(EmpaticaNode p) {
            super(p);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Arrays.asList((Object) "accX", "accY", "accZ");
        }
    }

    public static class IBI extends EmpaticaSensor {
        public IBI(EmpaticaNode p) {
            super(p);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "IBI");
        }
    }

    public static class Thermometer extends EmpaticaSensor {
        public Thermometer(EmpaticaNode p) {
            super(p);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "Temperature");
        }
    }

    public static class GSR extends EmpaticaSensor {
        public GSR(EmpaticaNode p) {
            super(p);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "GSR");
        }
    }

    public static class BVP extends EmpaticaSensor {
        public BVP(EmpaticaNode p) {
            super(p);
        }

        @Override
        public List<Object> getValueDescriptor() {
            return Collections.singletonList((Object) "BVP");
        }
    }
}
