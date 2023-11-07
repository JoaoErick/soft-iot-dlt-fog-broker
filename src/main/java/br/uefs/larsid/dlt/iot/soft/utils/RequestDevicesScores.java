package br.uefs.larsid.dlt.iot.soft.utils;

import java.util.List;
import java.util.logging.Logger;

import br.uefs.larsid.dlt.iot.soft.entity.Device;
import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;

public class RequestDevicesScores implements Runnable {

    /*-------------------------Constants---------------------------------------*/
    private final int QOS = 1;
    /*-------------------------------------------------------------------------*/

    private static final Logger logger = Logger.getLogger(RequestDevicesScores.class.getName());

    private boolean debugModeValue;
    private MQTTClient MQTTClientHost;
    private List<Device> devices;
    private Thread thread;
    /**
     * Builder method.
     *
     * @param controllerImpl Controller - Controller that will make use of this Listener.
     * @param MQTTClientHost MQTTClient - Gateway's own MQTT client.
     * @param debugModeValue boolean - How to debug the code.
     * @param devices List<Device> - virtual devices list.
     */
    public RequestDevicesScores(
        MQTTClient MQTTClientHost,
        boolean debugModeValue,
        List<Device> devices
    ) {
        this.MQTTClientHost = MQTTClientHost;
        this.debugModeValue = debugModeValue;
        this.devices = devices;
    }

    public void requestDevicesScores() {
        String message = "GET SCORE device";

        try {
            for (int i = 0; i < this.devices.size(); i++) {
                String publishTopic = String.format("dev/%s", this.devices.get(i).getId());

                this.MQTTClientHost.publish(publishTopic, message.getBytes(), QOS);
            }

        } catch (Exception  e) {
            printlnDebug("!Error when requesting device scores!");
        }
    
    }

    private void printlnDebug(String str) {
        if (this.debugModeValue) {
          logger.info(str);
        }
    }

    public void startRequester() {
        if (thread == null || !thread.isAlive()) {
            this.thread = new Thread(this);
            this.thread.setName("THREAD_REQUESTER");
            this.thread.start();
        }
    }

    @Override
    public void run() {
        this.requestDevicesScores();
    }

}
