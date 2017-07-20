package eu.fbk.mpba.sensorflow.sense;

import java.util.ArrayList;
import java.util.Collections;

import eu.fbk.mpba.sensorflow.Input;
import eu.fbk.mpba.sensorflow.InputGroup;

class InputGroupImpl implements InputGroup {
    private final ArrayList<Input> children = new ArrayList<>();
    private String name;

    InputGroupImpl(String name) {
        this.name = name;
    }

    synchronized void addChild(Input child) {
        children.add(child);
    }

    @Override
    public synchronized void onCreate() {
        children.forEach(Input::onCreate);
    }

    @Override
    public synchronized void onStart() {
        children.forEach(Input::onStart);
    }

    @Override
    public synchronized void onStop() {
        children.forEach(Input::onStop);
    }

    @Override
    public synchronized void onClose() {
        children.forEach(Input::onClose);
        children.clear();
    }

    @Override
    public synchronized Iterable<Input> getChildren() {
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
}