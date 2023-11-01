package br.uefs.larsid.dlt.iot.soft.utils;

import java.util.List;
import java.util.logging.Logger;

import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;

public class RequestDevicesScores {

    /*-------------------------Constants---------------------------------------*/
    private final int QOS = 1;
    /*-------------------------------------------------------------------------*/

    private static final Logger logger = Logger.getLogger(RequestDevicesScores.class.getName());

    private boolean debugModeValue;
    private MQTTClient MQTTClientHost;
    /**
     * Builder method.
     *
     * @param controllerImpl Controller - Controller that will make use of this Listener.
     * @param MQTTClientHost MQTTClient - Gateway's own MQTT client.
     * @param debugModeValue boolean - How to debug the code.
     */
    public RequestDevicesScores(
        MQTTClient MQTTClientHost,
        boolean debugModeValue
    ) {
        this.MQTTClientHost = MQTTClientHost;
        this.debugModeValue = debugModeValue;
    }

    public void requestDevicesScores(
        List<String> deviceIdsAuths
    ) {
        String message = "GET SCORE device";

        try {
            for (int i = 0; i < deviceIdsAuths.size(); i++) {
                String publishTopic = String.format("dev/%s", deviceIdsAuths.get(i));

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

}
