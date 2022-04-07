package br.uefs.larsid.dlt.iot.soft.model;

import br.uefs.larsid.dlt.iot.soft.mqtt.Listener;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerTopK;
import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;
import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerImpl implements Controller {

  /*-------------------------Constantes---------------------------------------*/
  private static final int QOS = 1;
  private static final String TOP_K_CLOUD = "TOP_K_HEALTH_CLOUD/#";
  private static final String TOP_K_CLOUD_RES = "TOP_K_HEALTH_CLOUD_RES/";
  private static final String TOP_K_FOG = "TOP_K_HEALTH_FOG/#";
  private static final String TOP_K_FOG_RES = "TOP_K_HEALTH_FOG_RES/";
  private static final String TOP_K_BOTTOM_RES = "TOP_K_HEALTH_RES/#";
  private static final String INVALID_TOP_K_FOG = "INVALID_TOP_K_FOG/";
  private static final String INVALID_TOP_K = "INVALID_TOP_K/#";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private MQTTClient mqttClientEdge;
  private MQTTClient mqttClientFog;
  private String childs;
  public Map<String, Map<String, Integer>> topKScores = new HashMap<String, Map<String, Integer>>();

  public ControllerImpl() {}

  /**
   * Inicialização do Bundle.
   */
  public void start() {
    this.mqttClientEdge.connect();
    this.mqttClientFog.connect();

    new Listener(this, mqttClientEdge, INVALID_TOP_K, QOS, debugModeValue);
    new Listener(this, mqttClientEdge, TOP_K_BOTTOM_RES, QOS, debugModeValue);
    new ListenerTopK(
      this,
      mqttClientFog,
      mqttClientEdge,
      TOP_K_FOG,
      QOS,
      debugModeValue
    );
  }

  /**
   * Finalização do Bundle.
   */
  public void stop() {
    this.mqttClientEdge.disconnect();
    this.mqttClientFog.disconnect();
    // TODO Desinscrever dos tópicos.
  }

  /**
   * Calcula o Top-K dos Top-Ks recebidos.
   */
  @Override
  public void calculateTopK(String id) {
    // TODO Colocar k como parâmetro
    // TODO Colocar verificação de childs
    printlnDebug("Waiting for Gateway nodes to send their Top-K");
    printlnDebug("OK... now let's calculate the TOP-K of TOP-K's!");
    printlnDebug("TOP_K Scores Received: " + this.getMapById(id).size());

    Map<String, Integer> devicesAndScoresMap = this.getMapById(id);

    devicesAndScoresMap
      .entrySet()
      .stream()
      .sorted(
        Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
      );

    printlnDebug("Top-K Result => " + devicesAndScoresMap.toString());
    printlnDebug("==== Fog gateway -> Fog UP gateway  ====");

    byte[] payload = devicesAndScoresMap.toString().getBytes();

    mqttClientFog.publish(TOP_K_FOG_RES + id, payload, 1);

    this.removeRequest(id);
  }

  /**
   *
   */
  @Override
  public Map<String, Map<String, Integer>> getTopKScores() {
    return this.topKScores;
  }

  /**
   *
   */
  @Override
  public Map<String, Integer> getMapById(String id) {
    return this.topKScores.get(id);
  }

  /**
   *
   */
  @Override
  public boolean putScores(String id, Map<String, Integer> fogMap) {
    return this.topKScores.put(id, fogMap).isEmpty();
  }

  /**
   *
   */
  @Override
  public Map<String, Integer> convertStrigToMap(String mapAsString) {
    return Arrays
      .stream(mapAsString.substring(1, mapAsString.length() - 1).split(", "))
      .map(entry -> entry.split("="))
      .collect(
        Collectors.toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1]))
      );
  }

  @Override
  public void sendInvalidTopKMessage(String topicId, String message) {
    printlnDebug(message);

    mqttClientFog.publish(INVALID_TOP_K_FOG + topicId, message.getBytes(), QOS);
    
  }

  /**
   *
   * @param id
   */
  @Override
  public void removeRequest(String id) {
    this.topKScores.remove(id);
  }

  public String getChilds() {
    return childs;
  }

  public void setChilds(String childs) {
    this.childs = childs;
  }

  public boolean isDebugModeValue() {
    return this.debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }

  public MQTTClient getMqttClientFog() {
    return this.mqttClientFog;
  }

  public void setMqttClientFog(MQTTClient mqttClientFog) {
    this.mqttClientFog = mqttClientFog;
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }

  public void setTopKScores(Map<String, Map<String, Integer>> topKScores) {
    this.topKScores = topKScores;
  }

  public MQTTClient getMqttClientEdge() {
    return this.mqttClientEdge;
  }

  public void setMqttClientEdge(MQTTClient mqttClientEdge) {
    this.mqttClientEdge = mqttClientEdge;
  }

  @Override
  public void sendEmptyTopK(String topicId) {
    byte[] payload = new HashMap<String, Map<String, Integer>>()
      .toString()
      .getBytes();

    this.mqttClientFog.publish(TOP_K_FOG_RES + topicId, payload, QOS);
  }
}
