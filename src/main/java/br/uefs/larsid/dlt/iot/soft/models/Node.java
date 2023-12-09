package br.uefs.larsid.dlt.iot.soft.models;

// import br.uefs.larsid.extended.mapping.devices.services.IDevicePropertiesManager;

import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import br.uefs.larsid.dlt.iot.soft.entities.Device;
import br.uefs.larsid.dlt.iot.soft.entities.Sensor;
import br.uefs.larsid.dlt.iot.soft.services.INode;
import br.uefs.larsid.dlt.iot.soft.tasks.CheckDevicesTask;

public class Node implements INode {
    private List<Device> devices;
    
    private int checkDeviceTaskTime;
    private Timer checkDeviceTimer;

    private String deviceAPIAddress;

    private boolean debugModeValue;
    private static final Logger logger = Logger.getLogger(Node.class.getName());

    public Node() {
    }

    public void start() {
        this.devices = new ArrayList<>();
        this.checkDeviceTimer = new Timer();
        this.checkDeviceTimer.scheduleAtFixedRate(
            new CheckDevicesTask(this),
            0,
            this.checkDeviceTaskTime * 1000
        );
    }

    /**
   * Executa o que foi definido na função quando o bundle for finalizado.
   */
    public void stop() {
        if (this.checkDeviceTimer != null) {
            this.checkDeviceTimer.cancel();
        }
    }

    /**
     * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
     *
     * @param strDevices String - Dispositivos requisitados.
     */
    public void loadConnectedDevices() {
        List<Device> devicesTemp = new ArrayList<Device>();

        try {
            JSONArray jsonArrayDevices = new JSONArray(
                ClientIotService.getApiIot(this.deviceAPIAddress)
            );

            for (int i = 0; i < jsonArrayDevices.length(); i++) {
                JSONObject jsonDevice = jsonArrayDevices.getJSONObject(i);
                ObjectMapper mapper = new ObjectMapper();
                Device device = mapper.readValue(
                    jsonDevice.toString(), 
                    Device.class
                );

                devicesTemp.add(device);

                List<Sensor> tempSensors = new ArrayList<Sensor>();
                JSONArray jsonArraySensors = jsonDevice.getJSONArray(
                    "sensors"
                );

                for (int j = 0; j < jsonArraySensors.length(); j++) {
                    JSONObject jsonSensor = jsonArraySensors.getJSONObject(j);
                    Sensor sensor = mapper.readValue(
                        jsonSensor.toString(), 
                        Sensor.class
                    );
                    sensor.setdeviceAPIAddress(deviceAPIAddress);
                    tempSensors.add(sensor);
                }

                device.setSensors(tempSensors);
            }
        } catch (JsonParseException e) {
            printlnDebug("Verify the correct format of 'DevicesConnected' property in configuration file.");
            logger.log(Level.SEVERE, null, e);
        } catch (JsonMappingException e) {
            printlnDebug(
                    "Verify the correct format of 'DevicesConnected' property in configuration file.");
            logger.log(Level.SEVERE, null, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }

        this.devices = devicesTemp;

        printlnDebug("Amount of devices connected: " + this.devices.size());
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public int getCheckDeviceTaskTime() {
        return checkDeviceTaskTime;
    }

    public void setCheckDeviceTaskTime(int checkDeviceTaskTime) {
        this.checkDeviceTaskTime = checkDeviceTaskTime;
    }

    public String getDeviceAPIAddress() {
        return deviceAPIAddress;
    }

    public void setDeviceAPIAddress(String deviceAPIAddress) {
        this.deviceAPIAddress = deviceAPIAddress;
    }

    public boolean isDebugModeValue() {
        return this.debugModeValue;
    }

    public void setDebugModeValue(boolean debugModeValue) {
        this.debugModeValue = debugModeValue;
    }

    private void printlnDebug(String str) {
        if (debugModeValue) {
            logger.info(str);
        }
    }
}
