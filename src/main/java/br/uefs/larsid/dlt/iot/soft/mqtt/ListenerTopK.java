package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerTopK implements IMqttMessageListener {

  private boolean debugModeValue;
  private Controller impl;
  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientHost;

  /**
   *
   * @param impl
   * @param clienteMQTT_Up
   * @param clienteMQTT_Host
   * @param topico
   * @param qos
   */
  public ListenerTopK(
    Controller impl,
    MQTTClient MQTTClientUp,
    MQTTClient MQTTClientHost,
    String topic,
    int qos
  ) {
    this.MQTTClientUp = MQTTClientUp;
    this.MQTTClientHost = MQTTClientHost;
    this.MQTTClientUp.subscribe(qos, this, topic);
    this.impl = impl;
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");

    /** [0] -> TOP_K_HEALTH; [1] -> Id-Request; [2] -> K */
    final int k = Integer.valueOf(params[2]);

    printlnDebug("==== Fog UP gateway -> Fog gateway  ====");

    printlnDebug("Requisição recebida: " + topic);

    if (params[0].equals("TOP_K_HEALTH")) {
      byte[] messageEmpty = "".getBytes();

      MQTTClientHost.publish(topic, messageEmpty, 1);

      Map<String, Integer> scoreMapEmpty = new HashMap<String, Integer>();

      impl.getTopKScores().put(params[1], scoreMapEmpty);

      // Inicia o cálculo de top k
      impl.calculateTopK(params[1], k);
    }
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }
    
}
