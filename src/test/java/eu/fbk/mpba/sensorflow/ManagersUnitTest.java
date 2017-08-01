package eu.fbk.mpba.sensorflow;

import org.junit.Test;

public class ManagersUnitTest {
    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_output_onStopAndClose() {
        OutputManager o = new OutputManager(new MockOutput("Out"),
                (sender, status) -> { }, true);

        o.onStopAndClose();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_output_onCreateAndStart() {
        OutputManager o = new OutputManager(new MockOutput("Out"),
                (sender, status) -> { }, true);

        o.onCreateAndAdded("HiPedro");
        o.onCreateAndAdded("HiPedro");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_input_onCreate() {
        InputManager o = new InputManager(new MockInput(null, "In"),
                (sender, status) -> { });

        o.onCreate();
        o.onCreate();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_input_onAdded() {
        InputManager o = new InputManager(new MockInput(null, "In"),
                (sender, status) -> { });

        o.onAdded();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_OutOfPlaceEvents_input_onRemovedAndClose() {
        InputManager o = new InputManager(new MockInput(null, "In"),
                (sender, status) -> { });

        o.onRemovedAndClose();
    }
}
