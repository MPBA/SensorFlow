package eu.fbk.mpba.sensorsflows.base;

public class Booleaned<T> {
    private boolean _trueness;
    private T _object;

    public Booleaned(T obj, boolean trueness) {
        _object = obj;
        _trueness = trueness;
    }

    public boolean isTrue() {
        return _trueness;
    }

    public void setTrue(boolean trueness) {
        _trueness = _trueness;
    }

    public T getObject() {
        return _object;
    }

    public void setObject(T _object) {
        _object = _object;
    }
}
