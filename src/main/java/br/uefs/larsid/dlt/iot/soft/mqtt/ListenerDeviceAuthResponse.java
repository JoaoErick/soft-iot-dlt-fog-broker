package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ListenerDeviceAuthResponse implements IMqttMessageListener {
  /*-------------------------Constants---------------------------------------*/
  private static final String AUTHENTICATED_DEVICES_RES = "AUTHENTICATED_DEVICES_RES";
  /*-------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private MQTTClient MQTTClientHost;
  private Controller controllerImpl;
  private static final Logger logger = Logger.getLogger(ListenerDeviceAuthResponse.class.getName());
  /**
   * Builder method.
   *
   * @param controllerImpl Controller - Controller that will make use of this Listener.
   * @param MQTTClientHost MQTTClient - Gateway's own MQTT client.
   * @param topics         String[] - Topics to be subscribed to.
   * @param qos            int - Quality of service of the topic that will be heard.
   * @param debugModeValue boolean - How to debug the code.
   */
  public ListenerDeviceAuthResponse(
    Controller controllerImpl,
    MQTTClient MQTTClientHost,
    String[] topics,
    int qos,
    boolean debugModeValue
  ) {
    this.MQTTClientHost = MQTTClientHost;
    this.controllerImpl = controllerImpl;
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
        case AUTHENTICATED_DEVICES_RES:
            printlnDebug("(Fog Broker) Receiving the list of authenticated devices...");

            JsonObject jsonGetTopKDown = new Gson()
            .fromJson(mqttMessage, JsonObject.class);

            String messageContent = jsonGetTopKDown.get("authDevices").getAsString();

            this.controllerImpl.setDeviceIdsAuths(
                Arrays.stream(messageContent.replaceAll("[\\[\\]\\s]", "").split(","))
                .collect(Collectors.toList())
            );

            printlnDebug(
              "(Fog Broker) Autheticated Devices Received = " + 
              this.controllerImpl.getDeviceIdsAuths().toString() + 
              "\n"
            );

            this.controllerImpl.calculateTopKDown(jsonGetTopKDown);
            
            break;
        default:
          String responseMessage = String.format(
            "\nOops! the request isn't recognized...\nTry one of the options below:\n- %s\n",
            AUTHENTICATED_DEVICES_RES
          );
  
          printlnDebug(responseMessage);
  
          break;
    }

  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      logger.info(str);
    }
  }
}