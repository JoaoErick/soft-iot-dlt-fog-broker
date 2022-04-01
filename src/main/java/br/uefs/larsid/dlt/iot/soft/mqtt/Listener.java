package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Listener implements IMqttMessageListener {

  private static final String TOP_K_RES = "TOP_K_HEALTH_RES";

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;

  public Listener(
      Controller controllerImpl,
      MQTTClient MQTTClientHost,
      String topic,
      int qos) {
    this.controllerImpl = controllerImpl;
    this.MQTTClientHost = MQTTClientHost;

    this.MQTTClientHost.subscribe(qos, this, topic);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");

    if (params[0].equals(TOP_K_RES)) {
      printlnDebug("==== Bottom gateway -> Fog gateway  ====");

      String messageContent = new String(message.getPayload());
      Map<String, Integer> bottomMap = this.convertWithStream(messageContent);
      Map<String, Integer> fogMap = this.controllerImpl.getMapById(params[1]);
      
      fogMap.putAll(bottomMap);
      controllerImpl.putScores(params[1], fogMap); 

      printlnDebug(
        "Top-k response received: " +
        controllerImpl.getMapById(params[1]).toString()
      );
    }
  }

  private Map<String, Integer> convertWithStream(String mapAsString) {
    Map<String, Integer> map = Arrays.stream(mapAsString.split(","))
      .map(entry -> entry.split("="))
      .collect(Collectors.toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1])));
    return map;
  }

  private void printlnDebug(String str) {
    if (isDebugModeValue()) {
      System.out.println(str);
    }
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}
