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
  private static final String TOP_K = "TOP_K_HEALTH_FOG/#";
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES/#";
  private static final String INVALID_TOP_K = "INVALID_TOP_K/#";
  private static final String TOP_K_RES_FOG = "TOP_K_HEALTH_RES_FOG/";
  private static final String INVALID_TOP_K_FOG = "INVALID_TOP_K_FOG/";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private MQTTClient MQTTClientHost;
  private MQTTClient MQTTClientUp;
  private String childs;
  public Map<String, Map<String, Integer>> topKScores = new HashMap<String, Map<String, Integer>>();

  public ControllerImpl() {}

  /**
   * Inicialização do Bundle.
   */
  public void start() {
    this.MQTTClientHost.connect();
    this.MQTTClientUp.connect();

    new Listener(this, MQTTClientHost, INVALID_TOP_K, QOS, debugModeValue);
    new Listener(this, MQTTClientHost, TOP_K_RES, QOS, debugModeValue);
    new ListenerTopK(
      this,
      MQTTClientUp,
      MQTTClientHost,
      TOP_K,
      QOS,
      debugModeValue
    );
  }

  /**
   * Finalização do Bundle.
   */
  public void stop() {
    this.MQTTClientHost.disconnect();
    this.MQTTClientUp.disconnect();
    // TODO Desinscrever dos tópicos.
  }

  /**
   * Calcula o Top-K dos Top-Ks recebidos.
   */
  @Override
  public void calculateTopK(String id, int k) {
    printlnDebug(
      "Waiting for Gateway nodes to send their top-" +
      k +
      " | " +
      "amount of nodes: " +
      this.getMapById(id).size()
    );

    // while ((this.getMapById(id).size() / k) < Integer.parseInt(this.childs)) {}
    while (this.getMapById(id).size() <= 0) {}

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

    MQTTClientUp.publish(TOP_K_RES_FOG + id, payload, 1);

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
      .stream(mapAsString.substring(1, mapAsString.length() - 1).split(","))
      .map(entry -> entry.split("="))
      .collect(
        Collectors.toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1]))
      );
  }

  @Override
  public void sendInvalidTopKMessage(String topicId, String message) {
    printlnDebug(message);

    MQTTClientUp.publish(INVALID_TOP_K_FOG + topicId, message.getBytes(), QOS);
  }

  /**
   *
   * @param id
   */
  private void removeRequest(String id) {
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

  public MQTTClient getMQTTClientUp() {
    return this.MQTTClientUp;
  }

  public void setMQTTClientUp(MQTTClient MQTTClientUp) {
    this.MQTTClientUp = MQTTClientUp;
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }

  public void setTopKScores(Map<String, Map<String, Integer>> topKScores) {
    this.topKScores = topKScores;
  }

  public MQTTClient getMQTTClientHost() {
    return this.MQTTClientHost;
  }

  public void setMQTTClientHost(MQTTClient mQTTClientHost) {
    this.MQTTClientHost = mQTTClientHost;
  }
}
