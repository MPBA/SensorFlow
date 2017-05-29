package eu.fbk.mpba.sensorsflows.sense;

import eu.fbk.mpba.sensorsflows.Flow;
import eu.fbk.mpba.sensorsflows.Input;


public class PassiveFlow extends Flow {

    private String[] header;
    private String name;
    private boolean on;

    PassiveFlow(Input parent, String header, String name) {
        this(parent, new String[] { header }, name);
    }

    PassiveFlow(Input parent, String[] header, String name) {
        super(parent);
        this.header = header;
        this.name = name;
    }

    public boolean isOn() {
        return on;
    }

    public boolean onValue(double value) {
        super.onValue(getTimeSource().getMonoUTCNanos(), new double[] { value });
        return on;
    }

    public boolean onLog(String message) {
        super.onLog(getTimeSource().getMonoUTCNanos(), message);
        return on;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getHeader() {
        return header;
    }

    @Override
    public void switchOn() {
        on = true;
    }

    @Override
    public void switchOff() {
        on = false;
    }
}
