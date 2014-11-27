package eu.fbk.mpba.sensorsflows.base;

/**
 * Main interface to represent a link between ISensor and IOutput
 */
public interface ILink<InputT, OutputT> {
    public InputT getInput();
    public OutputT getOutput();
    public boolean getEnabled();
    public void setEnabled(boolean enable);
}
