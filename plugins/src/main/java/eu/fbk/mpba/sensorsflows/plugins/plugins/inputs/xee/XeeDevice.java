package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.dquid.xee.driver.DQDriver;
import com.dquid.xee.driver.DQDriverEventListener;
import com.dquid.xee.driver.DQSourceType;
import com.dquid.xee.sdk.DQAccelerometerData;
import com.dquid.xee.sdk.DQCar;
import com.dquid.xee.sdk.DQData;
import com.dquid.xee.sdk.DQGpsData;
import com.dquid.xee.sdk.DQListenerInterface;
import com.dquid.xee.sdk.DQUnitManager;
import com.dquid.xee.sdk.DQUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;
import eu.fbk.mpba.sensorsflows.base.IMonoTimestampSource;
import eu.fbk.mpba.sensorsflows.util.ReadOnlyIterable;

public class XeeDevice implements DevicePlugin<Long, double[]>, DQListenerInterface, DQDriverEventListener, IMonoTimestampSource {

    private boolean receivingData;
    private BluetoothDevice deviceToConnect;
    private boolean firmwareUPDRequested;
    private boolean connectionRequested;
    private boolean flowing = false;
    private boolean connectionCheck = false;
    private boolean databaseCheck = false;

    protected boolean debug = true;
    protected final String debugTAG = "XeeALE";
    protected DevicePlugin<Long, double[]> parent;
    protected XeeSensor.XeeAccelerometer xeeAcc;
    protected XeeSensor.XeeGPS xeeGPS;
    protected List<SensorComponent<Long, double[]>> sensors;
    protected Map<String, XeeSensor.CarData> namesMap = new HashMap<>(50);
    protected Map<Long, XeeSensor.CarData> idMap = new HashMap<>(50);
    private ConnectionCallback ec = null;

    public interface ConnectionCallback {
        void error(int e, String message);
        void connected();
        void streaming();
        void ready();
    }

    protected void setDeviceToConnect(BluetoothDevice d) {
        deviceToConnect = d;
    }

    protected void setEnvironment(DQUtils.DQuidEnvs e) {
        if (debug)
            Log.v(debugTAG, "Set environment: " + e);
        DQUtils.setEnvironment(e);
    }

    /**
     * -1. check the bluetooth presence (2.1)
     *  1. set the bt device and environment (?)
     *  2. connection true
     */
    public XeeDevice() {
        if (debug)
            Log.v(debugTAG, "XeeDevice construction");

        xeeAcc = new XeeSensor.XeeAccelerometer(this);
        xeeGPS = new XeeSensor.XeeGPS(this);

        // Adding every possible sensor, every stream present in DQCar as DQData field.
        Field[] f = DQCar.class.getFields();
        sensors = new ArrayList<>(f.length);
        sensors.add(xeeAcc);
        sensors.add(xeeGPS);
        for (Field i : f)
            if (i.getType().equals(DQData.class)) {
                XeeSensor.CarData c = new XeeSensor.CarData(this, i.getName());
                namesMap.put(i.getName(), c);
                sensors.add(c);
            }

        DQDriver.INSTANCE.setEventListener(this);
        DQUnitManager.INSTANCE.addListener(this);

        setReceivingData(true);
        if (debug)
            Log.v(debugTAG, "XeeDevice inner construction done");
    }

    public XeeDevice(BluetoothDevice d, DQUtils.DQuidEnvs e, ConnectionCallback c, boolean simulation) {
        this();
        setDeviceToConnect(d);
        setEnvironment(e);
        ec = c;
        connect(simulation);
    }

    public void inputPluginInitialize(){
        if (debug)
            Log.v(debugTAG, "inputPluginInitialize");

        DQUnitManager.INSTANCE.addListener(this);
        flowing = true;
    }

    public void inputPluginFinalize() {
        if (debug)
            Log.v(debugTAG, "inputPluginFinalize");
        DQUnitManager.INSTANCE.removeListener(this);
        flowing = false;
    }

    @Override
    public String getName() {
        return deviceToConnect != null ? deviceToConnect.getName() : "UnknownXee";
    }

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return new ReadOnlyIterable<>(sensors.iterator());
    }

    // Device Connected Button
    protected void connect(boolean simulation) {
        if (debug)
            Log.v(debugTAG, "connection - connect: " + simulation);

        if (deviceToConnect != null || simulation) {
            connectionRequested = true;
            initDriver(simulation);
        } else {
            if (ec != null)
                ec.error(XeeSensor.EC_CONNECTION, "Device to connect to not set.");
        }
    }

    private void initDriver(Boolean simulation) {
        if (debug)
            Log.v(debugTAG, "initDriver - simulation: " + simulation);

        if(simulation){
            // DQDriver reads from a can trace (Simulator)
            DQDriver.INSTANCE.enableSource(DQSourceType.SIMULATOR_CAN_TRACE);
        } else {
            // DQDriver reads from bluetooth 2.1 (xee)
            if (deviceToConnect == null)
                Log.wtf(debugTAG, "deviceToConnect not set!");
            DQDriver.INSTANCE.setBtDevice(deviceToConnect);
            DQDriver.INSTANCE.enableSource(DQSourceType.BLUETOOTH_2_1);
        }
    }

    private void setReceivingData(final boolean receiving) {
        if (debug)
            Log.v(debugTAG, "setReceivingData - receiving: " + receiving);

        if (receivingData != receiving) {
            if (receiving) {
                DQUnitManager.INSTANCE.startReceivingCarData();
            } else {
                DQUnitManager.INSTANCE.stopReceivingCarData();
            }
            receivingData = receiving;
        }
    }

    private void disconnect() {
        if (debug)
            Log.v(debugTAG, "disconnect");
        // TODO Test
        setReceivingData(false);
        DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
        DQUnitManager.INSTANCE.disconnect();
    }

    @Override
    public void onConnectionSuccessful() {
        if (debug)
            Log.v(debugTAG, "Connection Successful");
        if (ec != null)
            ec.connected();
    }

    @Override
    public void onDisconnection() {
        if (debug)
            Log.v(debugTAG, "Disconnected");
        broadcastEvent(getMonoUTCNanos(System.nanoTime()), XeeSensor.EC_CONNECTION, "disconnected");
    }

    @Override
    public void onError(int arg0, String arg1) {
        if (debug)
            Log.v(debugTAG, "onError: " + arg1);

        if(arg0 == 401){
            Log.e(debugTAG, "ERROR 401!!!! " + arg1);
            DQUnitManager.INSTANCE.disconnect();
        }
        broadcastEvent(getMonoUTCNanos(System.nanoTime()), arg0, arg1);
    }

    @Override
    public void onDriverReady() {
        if (debug)
            Log.v(debugTAG, "onDriverReady");

        if (firmwareUPDRequested) {
            firmwareUPDRequested = false;
            DQUnitManager.INSTANCE.updateFirmware();
        } else
        if (connectionRequested) {
            connectionRequested = false;
//          if (serialNumberRequested) {
//              DQUnitManager.INSTANCE.getSerialNumber(); // TODO 8 no for now
//          } else {
            if (debug)
                Log.d(debugTAG, "Connecting the INSTANCE");
            DQUnitManager.INSTANCE.connect();
//          }
        } else {
            if (debug)
                Log.i(debugTAG, "...no fw nor conn requested");
            DQUnitManager.INSTANCE.checkFirmwareVersion();
        }
    }

    @Override
    public void onDriverDown(int arg0) {
        if(debug)
            Log.v(debugTAG, "onDriverDown - reason: " + arg0 + " - " + DQDriver.INSTANCE.dqdriverErrorDescriptions.get(arg0));

        if (ec != null)
            ec.error(arg0, DQDriver.INSTANCE.dqdriverErrorDescriptions.get(arg0));

        if(firmwareUPDRequested)
            initDriver(false);
    }

    @Override
    public void onNewAccelerometerData(DQAccelerometerData arg0) {
        Long ts = getMonoUTCNanos(System.nanoTime());
//        if(debug)
//            Log.v(debugTAG, "onNewAccelerometerData - " + arg0.toString());
        xeeAcc.sensorValue(ts, arg0);
    }

    @Override
    public void onNewGpsData(DQGpsData arg0) {
        Long ts = getMonoUTCNanos(System.nanoTime());
//        if(debug)
//            Log.v(debugTAG, "onNewGpsData - " + arg0.toString());
        if (!connectionCheck) {
            connectionCheck = true;
            if (ec != null)
                ec.streaming();
        }
        xeeGPS.sensorValue(ts, arg0);
    }

    @Override
    public void onNewData(HashMap<Long, DQData> arg0) {
//        if(debug)
//            Log.v(debugTAG, "onNewData - " + arg0.toString());
        Long ts = getMonoUTCNanos(System.nanoTime());
        if (!databaseCheck && arg0.size() != 0) {
            connectionCheck = true;
            if (ec != null)
                ec.ready();
        }
        if (flowing)
            for (DQData d : arg0.values()) {
                if (!idMap.containsKey(d.getId())) {
                        if (namesMap.containsKey(d.getName())) {
                            idMap.put(d.getId(), namesMap.get(d.getName()));
                            idMap.get(d.getId()).sendMeta(d);
                        }
                        else
                            Log.wtf(debugTAG, "Stream " + d.getId() + "-" + d.getName() + " was not put into the output sensors because was not in DQCar's DQData fields!");
                }
                idMap.get(d.getId()).sensorValue(ts, d);
            }

        // TODO 7: check the difference between arg0 and DQUnitManager.INSTANCE.getLastAvailable().
    }

    // Firmware update button
    protected void callFWUpdate(DQUtils.DQuidEnvs e) {
        if (deviceToConnect != null){
            firmwareUPDRequested = true;
            DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
        }
        else {
            broadcastEvent(getMonoUTCNanos(System.nanoTime()), 0, "disconnected");
        }
    }

    private long bootUTCNanos = System.currentTimeMillis() * 1_000_000L - System.nanoTime();

    @Override
    public long getMonoUTCNanos() {
        return System.nanoTime() + bootUTCNanos;
    }

    @Override
    public long getMonoUTCNanos(long realTimeNanos) {
        return bootUTCNanos + realTimeNanos;
    }

    private void broadcastEvent(long time, int code, String message) {
        if (flowing)
            for (SensorComponent<Long, double[]> i : getSensors())
                i.sensorEvent(time, code, message);
        else
            Log.i(debugTAG, "event: " + time + ", " + code + ", " + message);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public void close() {
        disconnect();
    }

    // unused

    @Override
    public void onDtcCodesAvailable(final ArrayList<String> arg0) {
        if(debug)
            Log.v(debugTAG, "DTC Codes: " + arg0.toString());
    }

    @Override
    public void onDtcNumberAvailable(final int arg0) {
        if (debug)
            Log.v(debugTAG, "DTC Number: " + arg0);
    }

    @Override
    public void onFirmwareUpdateCompleted() {
        if (debug)
            Log.v(debugTAG, "onFirmwareUpdateCompleted");
    }

    @Override
    public void onFirmwareUpdateIncrease(final double arg0) {
        if (debug)
            Log.v(debugTAG, "onFirmwareUpdateIncrease: " + arg0);
    }

    @Override
    public void onFirmwareUpdateNeeded(String versionAvailable) {
        if(debug)
            Log.v(debugTAG, "onFirmwareUpdateNeeded - version " + versionAvailable + " is available");

        firmwareUPDRequested = true;
        DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
    }

    @Override
    public void onFirmwareUpdateNotNeeded() {
        if(debug)
            Log.v(debugTAG, "onFirmwareUpdateNotNeeded");
    }

    @Override
    public void onFirmwareUpdateStarted() {
        if(debug)
            Log.v(debugTAG, "onFirmwareUpdateStarted");
    }

    @Override
    public void onFirmwareVersionObtained(final String arg0) {
        if(debug)
            Log.v(debugTAG, "onFirmwareVersionObtained: " + arg0);

    }

    @Override
    public void onSerialNumberObtained(final String arg0) {
        if(debug)
            Log.v(debugTAG, "onSerialNumberObtained: " + arg0);

        // TODO 8: return it to the user/event
    }

    @Override
    public void onCloseDataSessionAck() {

    }

    @Override
    public void onCloseDataSessionNack() {

    }

    @Override
    public void onOpenDataSessionAck() {

    }

    @Override
    public void onOpenDataSessionNack() {

    }

    @Override
    public void onSettingAck() {

    }

    @Override
    public void onSettingNack() {

    }
}
