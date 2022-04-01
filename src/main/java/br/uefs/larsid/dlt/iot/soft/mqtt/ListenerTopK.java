package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerTopK implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_FOG = "TOP_K_HEALTH_FOG";
  private static final String TOP_K = "TOP_K_HEALTH";
  private static final int QOS = 1;
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientHost;
  private Controller controllerImpl;

  /**
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientUp MQTTClient - Cliente MQTT do gateway superior.
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerTopK(
    Controller controllerImpl,
    MQTTClient MQTTClientUp,
    MQTTClient MQTTClientHost,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.MQTTClientUp = MQTTClientUp;
    this.MQTTClientHost = MQTTClientHost;
    this.controllerImpl = controllerImpl;
    this.debugModeValue = debugModeValue;
    this.MQTTClientUp.subscribe(qos, this, topic);
  }

  /**
   * 
   */
  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");

    final int k = Integer.valueOf(params[2]);

    printlnDebug("==== Fog UP gateway -> Fog gateway  ====");

    printlnDebug("Request received: " + topic);

    if (params[0].equals(TOP_K_FOG)) {
      byte[] messageEmpty = "".getBytes();

      String topicDown = String.format(
        "%s/%s/%s",
        TOP_K,
        params[1],
        params[2]
      );

      MQTTClientHost.publish(topicDown, messageEmpty, QOS);

      Map<String, Integer> scoreMapEmpty = new HashMap<String, Integer>();

      controllerImpl.getTopKScores().put(params[1], scoreMapEmpty);

      // Inicia o cálculo de Top-K
      controllerImpl.calculateTopK(params[1], k);
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