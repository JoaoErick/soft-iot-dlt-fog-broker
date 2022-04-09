package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerCloud implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_HEALTH_CLOUD = "TOP_K_HEALTH_CLOUD";
  private static final String TOP_K_HEALTH_FOG = "TOP_K_HEALTH_FOG";
  private static final String TOP_K_HEALTH_BOTTOM = "TOP_K_HEALTH";
  private static final int QOS = 1;
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient mqttClientCloud;
  private MQTTClient mqttClientFogBroker;
  private MQTTClient mqttClientBottomBroker;

  public ListenerCloud(
    Controller controllerImpl,
    MQTTClient mqttClientCloud,
    MQTTClient mqttClientFogBroker,
    MQTTClient mqttClientBottomBroker,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.debugModeValue = debugModeValue;
    this.controllerImpl = controllerImpl;
    this.mqttClientCloud = mqttClientCloud;
    this.mqttClientFogBroker = mqttClientFogBroker;
    this.mqttClientBottomBroker = mqttClientBottomBroker;

    this.mqttClientCloud.subscribe(qos, this, topic);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");

    final int k = Integer.valueOf(params[2]);

    printlnDebug("==== Cloud gateway -> Fog gateway  ====");

    printlnDebug("Request received: " + topic);

    if (k == 0) {
      printlnDebug("Top-K = 0");

      this.controllerImpl.sendEmptyTopK(params[1]);
    } else {
      switch (params[0]) {
        case TOP_K_HEALTH_CLOUD:
          byte[] messageEmpty = "".getBytes();

          String topicFogDown = String.format(
            "%s/%s/%s",
            TOP_K_HEALTH_FOG,
            params[1],
            params[2]
          );

          this.mqttClientFogBroker.publish(topicFogDown, messageEmpty, QOS);

          String topicBottom = String.format(
            "%s/%s/%s",
            TOP_K_HEALTH_BOTTOM,
            params[1],
            params[2]
          );

          mqttClientBottomBroker.publish(topicBottom, messageEmpty, QOS);

          Map<String, Integer> scoreMapEmpty = new HashMap<String, Integer>();

          controllerImpl.getTopKScores().put(params[1], scoreMapEmpty);
          break;
      }
    }
  }

  /**
   *
   * @param str
   */
  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }
}
