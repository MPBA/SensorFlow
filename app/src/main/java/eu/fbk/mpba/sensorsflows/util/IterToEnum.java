package eu.fbk.mpba.sensorsflows.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Allows to convert an iterator to an enumeration
 */
public class IterToEnum<E> implements Enumeration<E>{
    private Iterator<E> _;

    public IterToEnum(Iterator<E> i) {
        _ = i;
    }

    @Override
    public boolean hasMoreElements() {
        return _.hasNext();
    }

    @Override
    public E nextElement() {
        return _.next();
    }
}
