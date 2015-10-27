package eu.fbk.mpba.sensorsflows.base;

import java.util.Comparator;

public class StrongerComparator implements Comparator<Object> {
    @Override
    public int compare(Object o, Object t1) {
        int y = System.identityHashCode(o) - System.identityHashCode(t1);
        if (y == 0)
            if (o != t1) {
                if (o != null && t1 != null) {
                    int y2 = System.identityHashCode(o.getClass()) - System.identityHashCode(t1.getClass());
                    if (y2 == 0) {
                        int y3 = o.hashCode() - t1.hashCode();
                        if (y3 == 0) {
                            throw new IndistinguishableObjectsException(String.format(
                                    "Found two object with same class, same name and same hash that do not equal (==):%s, %s, %s",
                                    o.getClass().getName(), o.hashCode(), System.identityHashCode(o)));
                        } else
                            return y3;
                    } else
                        return y2;
                } else
                    throw new NullPointerException("null entry");
            } else
                return 0;
        else
            return y;
    }

    static class IndistinguishableObjectsException extends RuntimeException {
        IndistinguishableObjectsException(String message) {
            super(message);
        }
    }
}