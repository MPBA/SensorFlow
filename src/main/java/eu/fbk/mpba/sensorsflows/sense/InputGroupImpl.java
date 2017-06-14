package eu.fbk.mpba.sensorsflows.sense;

import java.util.ArrayList;
import java.util.Collections;

import eu.fbk.mpba.sensorsflows.Input;
import eu.fbk.mpba.sensorsflows.InputGroup;

abstract class InputGroupImpl implements InputGroup {
    private final ArrayList<Input> children = new ArrayList<>();
    boolean flowing = true;
    private String name;

    InputGroupImpl(String name) {
        this.name = name;
    }

    void addChild(Input child) {
        if (!flowing) {
            children.add(child);
        } else {
            throw new RuntimeException("Flowing");
        }
    }

    @Override
    public synchronized void onInputStart() {
        flowing = true;
        children.forEach(Input::turnOn);
        start();
    }

    @Override
    public synchronized void onInputStop() {
        children.forEach(Input::turnOff);
        children.clear();
        flowing = false;
        stop();
    }

    public abstract void start();

    public abstract void stop();

    @Override
    public Iterable<Input> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public String getSimpleName() {
        return name;
    }

    @Override
    public String getName() {
        return getSimpleName();
    }

    @Override
    public synchronized void close() {
        if (!flowing)
            children.forEach(Input::turnOff);
    }
}