package br.uefs.larsid.dlt.iot.soft.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListenerAuthenticatedDevices implements IMqttMessageListener {
    /*-------------------------Constants-------------------------------------*/
    private static final String TOPIC = "AUTHENTICATED_DEVICES";
    /*-----------------------------------------------------------------------*/

    private boolean debugModeValue;
    private Controller controllerImpl;
    private MQTTClient MQTTClientHost;
    private static final Logger logger = Logger.getLogger(
            ListenerAuthenticatedDevices.class.getName());

    /**
     * Constructor Method.
     *
     * @param controllerImpl Controller - Controller that will make use of this
     *                       Listener.
     * @param MQTTClientHost MQTTClient - Bottom Gateway MQTT Client.
     * @param topics         String[] - Topics that will be subscribed.
     * @param qos            int - Quality of service of the topic that will be
     *                       heard.
     * @param debugModeValue boolean - Mode to debug the code.
     */
    public ListenerAuthenticatedDevices(
            Controller controllerImpl,
            MQTTClient MQTTClientHost,
            String[] topics,
            int qos,
            boolean debugModeValue) {
        this.controllerImpl = controllerImpl;
        this.MQTTClientHost = MQTTClientHost;
        this.debugModeValue = debugModeValue;

        for (String topic : topics) {
            this.MQTTClientHost.subscribe(qos, this, topic);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        final String mqttMessage = new String(message.getPayload());

        printlnDebug("---- Hyperledger Bundle -> Fog Broker Bundle  ----");

        switch (topic) {
            case TOPIC:
                JsonObject jsonResponse = new Gson()
                        .fromJson(mqttMessage, JsonObject.class);

                String messageContent = jsonResponse.get("authDevices").getAsString();

                this.controllerImpl.getNode().setAuthenticatedDevicesIds(
                        Arrays.stream(messageContent.replaceAll("[\\[\\]\\s]", "").split(","))
                                .collect(Collectors.toList()));

                printlnDebug(
                        "(Fog Broker) Autheticated Devices Received = " +
                                this.controllerImpl.getNode().getAuthenticatedDevicesIds().toString() +
                                "\n");
                break;
            default:
                String responseMessage = String.format(
                        "\nOops! the request isn't recognized...\nTry one of the options below:\n- %s\n",
                        TOPIC);

                printlnDebug(responseMessage);

                break;
        }
    }

    public boolean isDebugModeValue() {
        return debugModeValue;
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