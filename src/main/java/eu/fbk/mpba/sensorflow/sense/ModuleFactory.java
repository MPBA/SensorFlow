package eu.fbk.mpba.sensorflow.sense;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ModuleFactory {
    public static <T extends Module> T newInstance(Class<T> klass, RequirementsProvider provider) {
        Constructor<?>[] constructors = klass.getConstructors();
        if (constructors.length > 0) {
            Class<?>[] parameterTypes = constructors[0].getParameterTypes();
            Object[] o = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                o[i] = provider.get(parameterTypes[i]);
            }
            try {
                return (T) constructors[0].newInstance(o);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        else
            return null;
    }

    public interface RequirementsProvider {
        <T> T get(Class<T> requirement);
    }
}
