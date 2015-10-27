package eu.fbk.mpba.sensorsflows.base;

import java.util.Comparator;

public class IStandardComparator implements Comparator<IStandard> {
    @Override
    public int compare(IStandard o, IStandard t1) {
        int y = System.identityHashCode(o) - System.identityHashCode(t1);
        if (y == 0)
            if (o != t1) {
                if (o != null && t1 != null) {
                    int y2 = System.identityHashCode(o.getClass()) - System.identityHashCode(t1.getClass());
                    if (y2 == 0) {
                        int y3 = System.identityHashCode(o.getName()) - System.identityHashCode(t1.getName());
                        if (y3 == 0) {
                            throw new IndistinguishableObjectsException(String.format(
                                    "Found two object with same class, same name and same hash that do not equal (==):%s, %s, %s",
                                    o.getClass().getName(), o.getName(), System.identityHashCode(o)));
                        } else
                            return y3;
                    } else
                        return y2;
                } else
                    throw new NullPointerException("Tried to add null device/output");
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