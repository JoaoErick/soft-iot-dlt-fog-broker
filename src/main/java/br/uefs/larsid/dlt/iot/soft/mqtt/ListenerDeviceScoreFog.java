package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.logging.Logger;

/**
 *
 * @author João Erick Barbosa
 */
public class ListenerDeviceScoreFog implements IMqttMessageListener {

  /*------------------------------ Constants ------------------------------*/ 
  private static Logger log = Logger.getLogger(ListenerDeviceScoreFog.class.getName());
  /*-------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;

  /**
   * Constructor Method.
   *
   * @param controllerImpl Controller - Controller that will make use of this Listener.
   * @param MQTTClientHost MQTTClient - Bottom Gateway MQTT Client.
   * @param topics         String[] - Topics that will be subscribed.
   * @param qos            int - Quality of service of the topic that will be heard.
   * @param debugModeValue boolean - Mode to debug the code.
   */
  public ListenerDeviceScoreFog(
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
    String msg = new String(message.getPayload());
    String deviceId;
    int score;

    JsonObject jsonProperties = new Gson().fromJson(msg, JsonObject.class);

    deviceId = jsonProperties.get("HEADER").getAsJsonObject().get("NAME").getAsString();
    score = jsonProperties.get("BODY").getAsJsonObject().get("score").getAsInt();

    this.controllerImpl.addDeviceScore(deviceId, score);
  }

  private void printlnDebug(String str) {
    if (isDebugModeValue()) {
      log.info(str);
    }
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}