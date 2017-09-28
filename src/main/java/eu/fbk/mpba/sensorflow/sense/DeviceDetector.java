package eu.fbk.mpba.sensorflow.sense;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * This interface define methods to recognize bluetooth device properties and type without
 * connecting to it.
 *
 * Each WirelessDevice should have a DeviceDetector that says if a given device is of that
 * WirelessDevice type.
 *
 * To return a Result, use the methods {isNot(), canBe(), shouldBe(), is()}.
 */

public abstract class DeviceDetector {
    private final Class<? extends WirelessDevice> type;

    public DeviceDetector(Class<? extends WirelessDevice> type) {
        this.type = type;
    }

    /**
     * As some manufacturers do not provide clear ways to recognize a device based on its properties
     * (without connecting to it) this enumeration lists three classes of confidence over the
     * results returned.
     *
     * See constants documentation for further information.
     */
    public enum Confidence {
        /**
         * The highest level of confidence. This level should be such that at most one DeviceDetector
         * recognizes a remote device as of its type.
         */
        IS(4),
        /**
         * A middle level where the device in analysis has a good chance to be of the reported type.
         * Higher levels o confidence have priority.
         */
        SHOULD_BE(2),
        /**
         * Last level of confidence. This should be used where the device has no way of being recognised
         * other than Bluetooth version.
         */
        CAN_BE(1),
        /**
         * This constant indicates that surely the device is not of the current type.
         */
        IS_NOT(0);

        int p;
        Confidence(int p) {
            this.p = p;
        }

        /**
         * Compares this confidence with another.
         * @param o Another confidence.
         * @return Returns true if this > o
         */
        public boolean greaterThan(Confidence o) {
            return this.p > o.p;
        }
    }

    /**
     * Given a list of WirelessDevice classes implementing the static method "getDeviceDetector",
     * finds the last SHOULD_BE or CAN_BE or the first IS
     * @param filter Collection of Class types referring to the Wireless Devices to scan.
     * @param i The object to classify.
     * @return Returns the chosen DeviceDetector.Result.
     */
    public static DeviceDetector.Result find(Collection<Class<? extends WirelessDevice>> filter, Object i) {
        DeviceDetector.Result found = null;
        for (Class<? extends WirelessDevice> f : filter) {
            DeviceDetector invoke;
            try {
                invoke = (DeviceDetector) f.getDeclaredMethod("getDeviceDetector").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                continue;
            }
            DeviceDetector.Result evaluate = invoke.evaluate(i);
            if (found == null || evaluate.getConfidence().greaterThan(found.getConfidence()))
                found = evaluate;
            if (found.getConfidence() == DeviceDetector.Confidence.IS)
                break;
        }
        return found;
    }

    public Result isNot() {
        return new Result(Confidence.IS_NOT, "", type);
    }

    public Result canBe(String name) {
        return new Result(Confidence.CAN_BE, name, type);
    }

    public Result shouldBe(String name) {
        return new Result(Confidence.SHOULD_BE, name, type);
    }

    public Result is(String name) {
        return new Result(Confidence.IS,name, type);
    }

    /**
     * This class contains the result of the evaluation ({@see evaluate()}).
     */
    public static class Result {
        private final Confidence c;
        private final String identifier;
        private Class<? extends WirelessDevice> type;

        private Result(Confidence c, String identifier, Class<? extends WirelessDevice> type) {
            this.c = c;
            this.identifier = identifier;
            this.type = type;
            this.type = type;
        }

        /**
         * Evaluation confidence.
         * @return Returns Confidence.IS_NOT if the device is not of the current type, or one of
         * Confidence.{{IS, SHOULD_BE, CAN_BE}}
         */
        public Confidence getConfidence() {
            return c;
        }

        /**
         * Identifier of the device to show to the user if the device is of the reported type.
         */
        public String getDeviceIdentifier() {
            return identifier;
        }

        /**
         * Type of the device detected.
         */
        public Class<? extends WirelessDevice> getType() {
            return type;
        }
    }

    /**
     * This method evaluates if the deviceObject is of the current type and with which confidence.
     * @param deviceObject An object representing the device. E.g. a BluetoothDevice
     * @return The result of the evaluation.
     */
    public abstract Result evaluate(Object deviceObject);
}
