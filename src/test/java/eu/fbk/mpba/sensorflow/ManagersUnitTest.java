package eu.fbk.mpba.sensorflow;

import org.junit.Test;

public class ManagersUnitTest {
    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_output_onStopAndClose() {
        OutputManager o = new OutputManager(new MockOutput("Out"),
                new OutputObserver() {
                    @Override
                    public void outputStatusChanged(OutputManager sender, PluginStatus status) {

                    }
                }, true);

        o.onStopAndClose();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_output_onCreateAndStart() {
        OutputManager o = new OutputManager(new MockOutput("Out"),
                new OutputObserver() {
                    @Override
                    public void outputStatusChanged(OutputManager sender, PluginStatus status) {

                    }
                }, true);

        o.onCreateAndAdded("HiPedro");
        o.onCreateAndAdded("HiPedro");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_input_onCreate() {
        InputManager o = new InputManager(new MockInput(null, "In"),
                new InputObserver() {
                    @Override
                    public void inputStatusChanged(InputManager input, PluginStatus state) {

                    }
                });

        o.onCreate();
        o.onCreate();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_input_onAdded() {
        InputManager o = new InputManager(new MockInput(null, "In"),
                new InputObserver() {
                    @Override
                    public void inputStatusChanged(InputManager input, PluginStatus state) {

                    }
                });

        o.onAdded();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_input_onRemovedAndClose() {
        InputManager o = new InputManager(new MockInput(null, "In"),
                new InputObserver() {
                    @Override
                    public void inputStatusChanged(InputManager input, PluginStatus state) {

                    }
                });

        o.onRemovedAndClose();
    }
}
