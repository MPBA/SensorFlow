package eu.fbk.mpba.sensorsflows.plugins.plugins.inputs.xee;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.dquid.xee.driver.DQDriver;
import com.dquid.xee.driver.DQDriverEventListener;
import com.dquid.xee.driver.DQSourceType;
import com.dquid.xee.sdk.DQAccelerometerData;
import com.dquid.xee.sdk.DQData;
import com.dquid.xee.sdk.DQGpsData;
import com.dquid.xee.sdk.DQListenerInterface;
import com.dquid.xee.sdk.DQUnitManager;
import com.dquid.xee.sdk.DQUtils;

import java.util.ArrayList;
import java.util.HashMap;

import eu.fbk.mpba.sensorsflows.DevicePlugin;
import eu.fbk.mpba.sensorsflows.SensorComponent;

public class XeeDevice implements DevicePlugin<Long, double[]>, DQListenerInterface, DQDriverEventListener {

    @Override
    public Iterable<SensorComponent<Long, double[]>> getSensors() {
        return null;
    }

    private boolean receivingData;
    private BluetoothDevice deviceToConnect;
    private boolean firmwareUPDRequested;
    private boolean connectionRequested;
    private boolean serialNumberRequested;

    protected boolean debug = true;
    protected final String debugTAG = "XeeALE";
    protected DevicePlugin<Long, double[]> parent;

    protected void setDeviceToConnect(BluetoothDevice d) {
        deviceToConnect = d;
    }

    protected void setEnvironment(DQUtils.DQuidEnvs e) {
        DQUtils.setEnvironment(e);
        // TODO 5: event
        // TO DO 3: check docu:: says nothing...
    }

    /**
     * -1. check the bluetooth presence (2.1)
     *  1. set the bt device and environment (?)
     *  2. connection true
     */
    public XeeDevice() {
        if (debug)
            Log.d(debugTAG, "XeeDevice construction");
        DQDriver.INSTANCE.setEventListener(this);
        DQUnitManager.INSTANCE.addListener(this);
        setReceivingData(true);
        if (debug)
            Log.d(debugTAG, "XeeDevice construction done");
    }

    public XeeDevice(BluetoothDevice d, DQUtils.DQuidEnvs e) {
        setDeviceToConnect(d);
        setEnvironment(e);
    }

    public void inputPluginInitialize(){
        if (debug)
            Log.d(debugTAG, "onResume");

        DQUnitManager.INSTANCE.addListener(this);
    }

    public void inputPluginFinalize() {
        if (debug)
            Log.d(debugTAG, "onPause");
        DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
    }

    @Override
    public String getName() {
        return ""; // TODO x: Add sense
    }

    // Device Connected Button
    protected void connection(boolean connect) {
        if (debug)
            Log.d(debugTAG, "connection - connect: " + connect);

        if (connect) {
            // WAS: simulator option
            if (deviceToConnect != null) {
                connectionRequested = true;
                initDriver(false);
            } else {
                // TODO 5: error or choose
            }

        } else {
            disconnectFromBd();
        }
    }

    void initDriver(Boolean simulation) {
        if (debug)
            Log.d(debugTAG, "initDriver - simulation: " + simulation);

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

    // Button firmware update clicked
    protected void callFWUpdate(DQUtils.DQuidEnvs e) {
        if (deviceToConnect != null){
            firmwareUPDRequested = true;
            DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
        }
        else {
            // TODO 6: log error
        }
    }

    private void setReceivingData(final boolean receiving) {
        if (debug)
            Log.d(debugTAG, "setReceivingData - receiving: " + receiving);

        if (receivingData != receiving) {
            if (receiving) {
                DQUnitManager.INSTANCE.startReceivingCarData();
            } else {
                DQUnitManager.INSTANCE.stopReceivingCarData();
            }
            receivingData = receiving;
        }
    }

    private void disconnectFromBd() {
        if (debug)
            Log.d(debugTAG, "disconnectFromBd");
        DQUnitManager.INSTANCE.disconnect();
    }

    @Override
    public void onConnectionSuccessful() {
        if (debug)
            Log.d(debugTAG, "Connection Successful");

//		updateUiOnConnection(true, -1);
        // TODO 5: event
    }

    @Override
    public void onDisconnection() {
        if (debug)
            Log.d(debugTAG, "Disconnected");

//		updateUiOnConnection(false, -1);
        // TODO 5: event
    }

    @Override
    public void onError(int arg0, String arg1) {
        final String completeDescr = "Error Occurred, code: " + arg0 + ": " + arg1;

        if (debug)
            Log.d(debugTAG, completeDescr);

        if(arg0 == 401){
            Log.d(debugTAG, "ERRORE!!!!");
            DQUnitManager.INSTANCE.disconnect();
            // TODO 7: check this if/code
        }

        // TODO 5: event
        //        runOnUiThread(new Runnable() {
        //
        //            @Override
        //            public void run() {
        //
        //                hideProgressWheel();
        //
        //                Toast.makeText(getApplicationContext(), completeDescr, Toast.LENGTH_LONG).show();
        //            }
        //        });
    }

    @Override
    public void onNewAccelerometerData(DQAccelerometerData arg0) {
        if(debug)
            Log.d(debugTAG, "onNewAccelerometerData - " + arg0.toString());
        // TODO 5: data sensor ACC
    }

    @Override
    public void onNewGpsData(DQGpsData arg0) {
        if(debug)
            Log.d(debugTAG, "onNewGpsData - " + arg0.toString());
        // TODO 5: data sensor GPS
    }

    @Override
    public void onNewData(HashMap<Long, DQData> arg0) {
        //noinspection unused
        HashMap<Long, DQData> dataHashMap = DQUnitManager.INSTANCE.getLastAvailable();
        // TODO 5: data sensor DATA
        // TODO 8: check the difference between arg0 and dataHashMap.
    }

    @Override
    public void onDriverDown(int arg0) {
        if(debug)
            Log.d(debugTAG, "onDriverDown - reason: " + arg0 + " - " + DQDriver.INSTANCE.dqdriverErrorDescriptions.get(arg0));

        if(firmwareUPDRequested)
            initDriver(false);
    }

    @Override
    public void onDriverReady() {
        // TODO 4: understand driver ready/down operation (check docu)
        if (debug)
            Log.d(debugTAG, "onDriverReady");

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
                Log.i(debugTAG, "!-! no fw nor conn requested");
            DQUnitManager.INSTANCE.checkFirmwareVersion();
        }
    }

    // unused

    @Override
    public void onDtcCodesAvailable(final ArrayList<String> arg0) {
        if(debug)
            Log.d(debugTAG, "DTC Codes: " + arg0.toString());
    }

    @Override
    public void onDtcNumberAvailable(final int arg0) {
        if (debug)
            Log.d(debugTAG, "DTC Number: " + arg0);
    }

    @Override
    public void onFirmwareUpdateCompleted() {
        if (debug)
            Log.d(debugTAG, "onFirmwareUpdateCompleted");
    }

    @Override
    public void onFirmwareUpdateIncrease(final double arg0) {
        if (debug)
            Log.d(debugTAG, "onFirmwareUpdateIncrease: " + arg0);
    }

    @Override
    public void onFirmwareUpdateNeeded(String versionAvailable) {
        if(debug)
            Log.d(debugTAG, "onFirmwareUpdateNeeded - version " + versionAvailable + " is available");

        firmwareUPDRequested = true;
        DQDriver.INSTANCE.disableSource(DQSourceType.BLUETOOTH_2_1);
    }

    @Override
    public void onFirmwareUpdateNotNeeded() {
        if(debug)
            Log.d(debugTAG, "onFirmwareUpdateNotNeeded");
    }

    @Override
    public void onFirmwareUpdateStarted() {
        if(debug)
            Log.d(debugTAG, "onFirmwareUpdateStarted");
    }

    @Override
    public void onFirmwareVersionObtained(final String arg0) {
        if(debug)
            Log.d(debugTAG, "onFirmwareVersionObtained: " + arg0);

    }

    @Override
    public void onSerialNumberObtained(final String arg0) {
        if(debug)
            Log.d(debugTAG, "onSerialNumberObtained: " + arg0);

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
