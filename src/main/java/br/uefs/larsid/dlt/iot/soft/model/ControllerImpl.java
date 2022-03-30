package br.uefs.larsid.dlt.iot.soft.model;

import br.uefs.larsid.dlt.iot.soft.mqtt.Listener;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerTopK;
import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;
import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerImpl implements Controller {

  private static final int QOS = 1;
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES/#";
  private static final String TOP_K = "TOP_K_HEALTH/#";

  private boolean debugModeValue;
  private MQTTClient MQTTClientHost;
  private MQTTClient MQTTClientUp;
  private String childs;
  public Map<String, List<String>> topKScores = new HashMap<String, List<String>>();

  public ControllerImpl() {}

  public void start() {
    this.MQTTClientHost.connect();
    this.MQTTClientUp.connect();

    new Listener(this, MQTTClientHost, TOP_K_RES, QOS);
    new ListenerTopK(this, MQTTClientUp, MQTTClientHost, TOP_K, QOS);
  }

  public void stop() {
    this.MQTTClientHost.disconnect();
    this.MQTTClientUp.disconnect();
    // Desinscrever dos tópicos.
  }

  @Override
  public void calculateTopK(String id, int k) {
    printlnDebug(
      "Waiting for Gateway nodes to send their top-" +
      k +
      " | " +
      "amount of nodes: " +
      this.getScoresById(id).size()
    );

    while (this.getScoresById(id).size() < Integer.parseInt(this.childs)) {}

    printlnDebug("OK... now let's calculate the TOP-K dos TOP-K's!");
    printlnDebug("TOP_K SCORES RECEIVED: " + this.getScoresById(id).size());

    Map<String, Integer> devicesAndScoresMap = new HashMap<String, Integer>();

    for (String result : this.getScoresById(id)) {
      result = result.replace("{", "");
      result = result.replace("}", "");
      result = result.replace(" ", "");

      String[] pairs = result.split(",");

      for (int i = 0; i < pairs.length; i++) {
        String pair = pairs[i];
        String[] keyValue = pair.split("=");
        devicesAndScoresMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
      }
    }

    devicesAndScoresMap
      .entrySet()
      .stream()
      .sorted(Map.Entry.<String, Integer>comparingByValue());

    /* Object[] a = devicesAndScoresMap.entrySet().toArray(); */

    /* Arrays.sort(
      a,
      new Comparator<Object>() {
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
          return ((Map.Entry<String, Integer>) o2).getValue()
            .compareTo(((Map.Entry<String, Integer>) o1).getValue());
        }
      }
    ); */
    Map<String, Integer> top_k = new HashMap<String, Integer>();

    // Pegando os k piores ...
    /* for (int i = 0; i < k; i++) {
      Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) a[i];
      top_k.put(e.getKey(), e.getValue());
    } */
    for (int i = 0; i < k; i++) {
      Map.Entry<String, Integer> e = devicesAndScoresMap[i];
      top_k.put(e.getKey(), e.getValue());
    }

    System.out.println("TOP_K => " + top_k.toString());
    System.out.println("==== Fog gateway -> Fog UP gateway  ====");
    byte[] b = top_k.toString().getBytes();
    //clienteMQTT_UP.publicar("TOP_K_HEALTH_RES/" + id, b, 1);

  }

  @Override
  public Map<String, List<String>> getTopKScores() {
    return this.topKScores;
  }

  @Override
  public List<String> getScoresById(String id) {
    return this.topKScores.get(id);
  }

  @Override
  public boolean putScores(String id, List<String> score) {
    List<String> list = this.topKScores.put(id, score);

    return list.isEmpty();
  }

  public String getChilds() {
    return childs;
  }

  public void setChilds(String childs) {
    this.childs = childs;
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }

  public Map<String, List<String>> getTopk_k_scoresByIdrequi() {
    return topKScores;
  }

  public void setTopk_k_scoresByIdrequi(Map<String, List<String>> topKScores) {
    this.topKScores = topKScores;
  }

  public MQTTClient getMQTTClientHost() {
    return MQTTClientHost;
  }

  public void setMQTTClientHost(MQTTClient mQTTClientHost) {
    MQTTClientHost = mQTTClientHost;
  }

  public MQTTClient getMQTTClientUp() {
    return MQTTClientUp;
  }

  public void setMQTTClientUp(MQTTClient mQTTClientUp) {
    MQTTClientUp = mQTTClientUp;
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }
}
