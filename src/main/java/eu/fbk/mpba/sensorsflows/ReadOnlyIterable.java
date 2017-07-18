package eu.fbk.mpba.sensorsflows;

import java.util.Iterator;

/**
 * Allows to convert an iterator to an enumeration
 */
public class ReadOnlyIterable<E> implements Iterable<E>{
    private Iterator<E> i;

    public ReadOnlyIterable(Iterator<E> i) {
        this.i = i;
    }

    /**
     * Returns an {@link java.util.Iterator} for the elements in this object.
     *
     * @return An {@code Iterator} instance.
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public E next() {
                return i.next();
            }

            /**
             * Removes the last object returned by {@code next} from the collection.
             * This method can only be called once between each call to {@code next}.
             *
             * @throws UnsupportedOperationException if removing is not supported by the collection being
             *                                       iterated.
             * @throws IllegalStateException         if {@code next} has not been called, or {@code remove} has
             *                                       already been called after the last call to {@code next}.
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Read Only collection.");
            }
        };
    }
}
