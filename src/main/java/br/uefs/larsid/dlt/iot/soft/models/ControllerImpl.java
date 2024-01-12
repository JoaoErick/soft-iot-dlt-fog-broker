package br.uefs.larsid.dlt.iot.soft.models;

import br.uefs.larsid.dlt.iot.soft.entities.Device;
import br.uefs.larsid.dlt.iot.soft.entities.Sensor;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerAuthenticatedDevices;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerConnection;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerDeviceScoreEdge;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerDeviceScoreFog;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerRequest;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerResponse;
import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;
import br.uefs.larsid.dlt.iot.soft.services.Controller;
import br.uefs.larsid.dlt.iot.soft.utils.ConvertValueToScore;
import br.uefs.larsid.dlt.iot.soft.utils.MapToArray;
import br.uefs.larsid.dlt.iot.soft.utils.RequestDevicesScores;
import br.uefs.larsid.dlt.iot.soft.utils.SortTopK;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Logger;

public class ControllerImpl implements Controller {

  /*-------------------------Constantes---------------------------------------*/
  private static final int QOS = 1;
  private static final String TOP_K_FOG = "GET topk";
  private static final String TOP_K = "TOP_K_HEALTH/#";
  private static final String SENSORS_FOG = "GET sensors";
  private static final String SENSORS = "SENSORS";
  private static final String TOP_K_RES_FOG = "TOP_K_HEALTH_FOG_RES/";
  private static final String SENSORS_FOG_RES = "SENSORS_FOG_RES/";
  private static final String SENSORS_RES = "SENSORS_RES/";
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES/#";
  private static final String INVALID_TOP_K = "INVALID_TOP_K/#";
  private static final String INVALID_TOP_K_FOG = "INVALID_TOP_K_FOG/";

  private static final String DEVICE_SCORE = "dev/+/score/RES";

  private static final String AUTHENTICATED_DEVICES = "AUTHENTICATED_DEVICES";

  private static final String CONNECT = "SYN";
  private static final String DISCONNECT = "FIN";
  /*--------------------------------------------------------------------------*/

  private Node node;

  private boolean hasNodes;
  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientHost;

  private Map<String, Map<String, Integer>> topKScores = new LinkedHashMap<String, Map<String, Integer>>();
  private Map<String, Integer> responseQueue = new LinkedHashMap<String, Integer>();
  private Map<String, Integer> devicesScores;
  private int amountDevicesScoresReceived = 0;

  private List<String> nodesUris;
  private int timeoutInSeconds;

  private JsonObject sensorsTypesJSON = new JsonObject();
  private JsonObject jsonGetTopK;

  private long responseTime;
  private boolean debugModeValue;
  private static final Logger logger = Logger.getLogger(ControllerImpl.class.getName());

  public ControllerImpl() {
  }

  /**
   * Inicializa o bundle.
   */
  public void start() {
    this.MQTTClientUp.connect();
    this.MQTTClientHost.connect();

    devicesScores = new LinkedHashMap<String, Integer>();
    if (hasNodes) {
      nodesUris = new ArrayList<>();
      String[] topicsRequest = { TOP_K_FOG, SENSORS_FOG };
      String[] topicsConnection = { CONNECT, DISCONNECT };
      String[] topicsResponse = { TOP_K_RES, INVALID_TOP_K, SENSORS_RES };
      String[] topicsDeviceScore = { DEVICE_SCORE };

      new ListenerRequest(
          this,
          MQTTClientUp,
          MQTTClientHost,
          this.nodesUris,
          topicsRequest,
          QOS,
          debugModeValue);
      new ListenerConnection(
          this,
          MQTTClientHost,
          topicsConnection,
          QOS,
          debugModeValue);
      new ListenerResponse(
          this,
          MQTTClientHost,
          topicsResponse,
          QOS,
          debugModeValue);
      new ListenerDeviceScoreFog(
          this,
          MQTTClientHost,
          topicsDeviceScore,
          QOS,
          debugModeValue);
    } else {
      String[] topics = { TOP_K, SENSORS };
      String[] topicsDeviceScore = { DEVICE_SCORE };

      new ListenerRequest(
          this,
          MQTTClientUp,
          MQTTClientHost,
          this.nodesUris,
          topics,
          QOS,
          debugModeValue);
      new ListenerDeviceScoreEdge(
          this,
          MQTTClientHost,
          topicsDeviceScore,
          QOS,
          debugModeValue);

    }

    if (!MQTTClientUp.getIp().equals(MQTTClientHost.getIp())) {
      byte[] payload = String
          .format("%s:%s", MQTTClientHost.getIp(), MQTTClientHost.getPort())
          .getBytes();
  
      this.MQTTClientUp.publish(CONNECT, payload, QOS);
    }

    String[] topicsAutheticatedDevices = { AUTHENTICATED_DEVICES };

    new ListenerAuthenticatedDevices(
          this,
          MQTTClientHost,
          topicsAutheticatedDevices,
          QOS,
          debugModeValue);
  }

  /**
   * Finaliza o bundle.
   */
  public void stop() {
    if (!this.hasNodes) {
      this.MQTTClientUp.unsubscribe(TOP_K);
      this.MQTTClientUp.unsubscribe(SENSORS);
      this.MQTTClientHost.unsubscribe(DEVICE_SCORE);
      this.MQTTClientHost.unsubscribe(AUTHENTICATED_DEVICES);
    } else {
      this.MQTTClientUp.unsubscribe(TOP_K_FOG);
      this.MQTTClientUp.unsubscribe(SENSORS_FOG);
      this.MQTTClientUp.unsubscribe(CONNECT);
      this.MQTTClientUp.unsubscribe(DISCONNECT);
      this.MQTTClientHost.unsubscribe(TOP_K_RES);
      this.MQTTClientHost.unsubscribe(INVALID_TOP_K);
      this.MQTTClientHost.unsubscribe(SENSORS_RES);
      this.MQTTClientHost.unsubscribe(DEVICE_SCORE);
      this.MQTTClientHost.unsubscribe(AUTHENTICATED_DEVICES);
    }

    if (!MQTTClientUp.getIp().equals("localhost")) {
      byte[] payload = String
            .format("%s:%s", MQTTClientHost.getIp(), MQTTClientHost.getPort())
            .getBytes();

      this.MQTTClientUp.publish(DISCONNECT, payload, QOS);
    }

    this.MQTTClientHost.disconnect();
    this.MQTTClientUp.disconnect();
  }

  /**
   * Calcula o score dos dispositivos conectados e autenticados.
   *
   * @return Map
   */
  @Override
  public Map<String, Integer> calculateScoresAuthenticatedDevices(JsonArray functionHealth) {
    Map<String, Integer> temp = new LinkedHashMap<String, Integer>();
    List<String> sensorsTypes = this.loadSensorsTypes();
    List<String> authenticatedDevicesTemp = this.getNode().getAuthenticatedDevicesIds();

    if (sensorsTypes.size() == functionHealth.size()) {
      for (int i = 0; i < functionHealth.size(); i++) {
        String sensorType = functionHealth
            .get(i)
            .getAsJsonObject()
            .get("sensor")
            .getAsString();

        /**
         * Caso o tipo de sensor especificado não exista nos dispositivos,
         * retorna um Map vazio.
         */
        if (!sensorsTypes.contains(sensorType)) {
          return new LinkedHashMap<String, Integer>();
        }
      }

      for (Device device : this.node.getDevices()) {
        if (authenticatedDevicesTemp.contains(device.getId())) {
          int score = 0;
          int sumWeight = 0;

          for (int i = 0; i < functionHealth.size(); i++) {
            String sensorType = functionHealth
                .get(i)
                .getAsJsonObject()
                .get("sensor")
                .getAsString();
            int weight = functionHealth
                .get(i)
                .getAsJsonObject()
                .get("weight")
                .getAsInt();

            Sensor sensor = device.getSensorBySensorType(sensorType);
            sensor.getValue(device.getId());
            score += ConvertValueToScore.calculateSensorScore(
                sensor.getType(),
                sensor.getValue()) * weight;
            sumWeight += weight;
          }

          temp.put(device.getId(), score / sumWeight);
        }
      }
    }

    return temp;
  }
  /**
   * Calcula o score dos dispositivos conectados.
   *
   * @return Map
   */
  @Override
  public Map<String, Integer> calculateScores(JsonArray functionHealth) {
    Map<String, Integer> temp = new LinkedHashMap<String, Integer>();
    List<String> sensorsTypes = this.loadSensorsTypes();

    if (sensorsTypes.size() == functionHealth.size()) {
      for (int i = 0; i < functionHealth.size(); i++) {
        String sensorType = functionHealth
            .get(i)
            .getAsJsonObject()
            .get("sensor")
            .getAsString();

        /**
         * Caso o tipo de sensor especificado não exista nos dispositivos,
         * retorna um Map vazio.
         */
        if (!sensorsTypes.contains(sensorType)) {
          return new LinkedHashMap<String, Integer>();
        }
      }

      for (Device device : this.node.getDevices()) {
        int score = 0;
        int sumWeight = 0;

        for (int i = 0; i < functionHealth.size(); i++) {
          String sensorType = functionHealth
              .get(i)
              .getAsJsonObject()
              .get("sensor")
              .getAsString();
          int weight = functionHealth
              .get(i)
              .getAsJsonObject()
              .get("weight")
              .getAsInt();

          Sensor sensor = device.getSensorBySensorType(sensorType);
          sensor.getValue(device.getId());
          score += ConvertValueToScore.calculateSensorScore(
              sensor.getType(),
              sensor.getValue()) * weight;
          sumWeight += weight;
        }

        temp.put(device.getId(), score / sumWeight);
      }
    }

    return temp;
  }

  /**
   * Publica o Top-K calculado para a camada de cima.
   *
   * @param id             String - Id da requisição.
   * @param k              int - Quantidade de scores requisitados.
   * @param functionHealth JsonArray - Array contendo a função de cálculo do
   *                       Top-K.
   */
  @Override
  public void publishTopK(String id, int k, JsonArray functionHealth) {
    printlnDebug("Waiting for Gateway nodes to send their Top-K");

    long start = System.currentTimeMillis();
    long end = start + this.timeoutInSeconds * 1000;

    if (this.hasNodes) {
      /*
       * Enquanto a quantidade de respostas da requisição for menor que o número
       * de nós filhos
       */
      while (this.responseQueue.get(id) < this.nodesUris.size() &&
          System.currentTimeMillis() < end) {
      }
    }

    if (!this.node.getDevices().isEmpty()) {
      printlnDebug("Requesting the real scores of the devices...");
      RequestDevicesScores requester = new RequestDevicesScores(
          MQTTClientHost,
          debugModeValue,
          this.node.getDevices());

      requester.startRequester();

      this.calculateTopKUp();

    } else {
      this.calculateGeneralTopK(id, k);
    }

  }

  /**
   * Publica os tipos de sensores para a camada de cima.
   */
  @Override
  public void publishSensorType() {
    printlnDebug("Waiting for Gateway nodes to send their sensors types");

    long start = System.currentTimeMillis();
    long end = start + this.timeoutInSeconds * 1000;

    /*
     * Enquanto a quantidade de respostas da requisição for menor que o número
     * de nós filhos
     */
    while (this.responseQueue.get("getSensors") < this.nodesUris.size() &&
        System.currentTimeMillis() < end) {
    }

    byte[] payload = sensorsTypesJSON.toString().replace("\\", "").getBytes();

    MQTTClientUp.publish(SENSORS_FOG_RES, payload, 1);

    this.removeSpecificResponse("getSensors");
  }

  /**
   * Requisita os tipos de sensores de um dispositivo conectado.
   *
   * @return List<String>
   */
  public List<String> loadSensorsTypes() {
    List<String> sensorsList = new ArrayList<>();

    for (Sensor sensor : this.node.getDevices().get(0).getSensors()) {
      sensorsList.add(sensor.getType());
    }

    return sensorsList;
  }

  /**
   * Retorna o mapa de requisições do sistema, composto pelo
   * id da requisição (chave) e o mapa de scores (valor).
   * O mapa de scores é composto pelo nome do dispositivo (Chave)
   * e o score (valor) associado.
   *
   * @return Map
   */
  @Override
  public Map<String, Map<String, Integer>> getTopKScores() {
    return this.topKScores;
  }

  /**
   * Retorna o mapa de scores de acordo com o id da requisição
   * passado por parâmetro.
   *
   * @param id String - Id da requisição.
   * @return Map
   */
  @Override
  public Map<String, Integer> getMapById(String id) {
    return this.topKScores.get(id);
  }

  /**
   * Adiciona um mapa de scores de uma nova requisição no mapa de
   * requisições na sua respectiva.
   *
   * @param id     String - Id da requisição.
   * @param fogMap Map - Mapa de requisições.
   */
  @Override
  public void putScores(String id, Map<String, Integer> fogMap) {
    if (this.topKScores.get(id) != null) {
      this.topKScores.get(id).putAll(fogMap);
    } else {
      this.topKScores.put(id, fogMap).isEmpty();
    }
  }

  /**
   * Adiciona os sensores em um JSON para enviar para a camada superior.
   *
   * @param jsonReceived JSONObject - JSON contendo os tipos dos sensores.
   */
  @Override
  public void putSensorsTypes(JsonObject jsonReceived) {
    if (this.sensorsTypesJSON.get("sensors").getAsString().equals("[]")) {
      sensorsTypesJSON = jsonReceived;
    }
  }

  /**
   * Envia uma mensagem indicando que o Top-K pedido possui uma quantidade
   * inválida.
   *
   * @param topicId String - Id da requisição do Top-K.
   * @param message String - Mensagem.
   */
  @Override
  public void sendInvalidTopKMessage(String topicId, String message) {
    printlnDebug(message);

    MQTTClientUp.publish(INVALID_TOP_K_FOG + topicId, message.getBytes(), QOS);
  }

  /**
   * Remove do mapa de requisições o id da requisição junto com mapa de scores
   * associado a ele.
   *
   * @param id String - Id da requisição.
   */
  @Override
  public void removeRequest(String id) {
    this.topKScores.remove(id);
  }

  /**
   * Cria uma nova chave no mapa de resposta dos filhos.
   *
   * @param id String - Id da requisição.
   */
  @Override
  public void addResponse(String id) {
    responseQueue.put(id, 0);
  }

  /**
   * Atualiza a quantidade de respostas.
   *
   * @param id String - Id da requisição.
   */
  @Override
  public void updateResponse(String id) {
    int temp = responseQueue.get(id);
    responseQueue.put(id, ++temp);
  }

  /**
   * Remove uma resposta específica da fila de respostas.
   *
   * @param id String - Id da requisição.
   */
  @Override
  public void removeSpecificResponse(String id) {
    responseQueue.remove(id);
  }

  /**
   * Envia um mapa vazio.
   *
   * @param topicId String - Id da requisição.
   */
  @Override
  public void sendEmptyTopK(String topicId) {
    byte[] payload = new LinkedHashMap<String, Map<String, Integer>>()
        .toString()
        .getBytes();

    this.MQTTClientUp.publish(TOP_K_RES_FOG + topicId, payload, QOS);
  }

  /**
   * Calcula o Top-k dos dispositivos na névoa.
   */
  public void calculateTopKUp() {
    printlnDebug("Calculating scores from devices...");

    int k;
    String id;
    JsonArray functionHealth;

    id = this.jsonGetTopK.get("id").getAsString();
    k = this.jsonGetTopK.get("k").getAsInt();
    functionHealth = this.jsonGetTopK.get("functionHealth").getAsJsonArray();

    /* Adicionando os dispositivos conectados em si mesmo. */
    this.putScores(id, this.calculateScoresAuthenticatedDevices(functionHealth));

    int topkMapSize = this.topKScores.get(id).size();

    if (topkMapSize < k) {
      printlnDebug("Insufficient Top-K!");

      this.sendInvalidTopKMessage(
          id,
          String.format(
              "Can't possible calculate the Top-%s, sending the Top-%d!",
              k,
              topkMapSize));
    }

    this.calculateGeneralTopK(id, k);
  }

  /**
   * Calcula o Top-k dos dispositivos na borda da rede.
   */
  public void calculateTopKDown() {
    printlnDebug("Calculating scores from devices...");

    int k;
    String id;
    JsonArray functionHealth;

    id = this.jsonGetTopK.get("id").getAsString();
    k = this.jsonGetTopK.get("k").getAsInt();
    functionHealth = this.jsonGetTopK.get("functionHealth").getAsJsonArray();

    Map<String, Integer> scores = new LinkedHashMap<String, Integer>();

    /**
     * Se não houver nenhum dispositivo conectado.
     */
    if (this.node.getDevices().isEmpty()) {
      printlnDebug("Sorry, there are no devices connected.");

      byte[] payload = scores.toString().getBytes();

      MQTTClientUp.publish(TOP_K_RES_FOG + id, payload, 1);
    } else {

      scores = this.calculateScoresAuthenticatedDevices(functionHealth);

      /*
       * Reordenando o mapa de Top-K (Ex: {device2=23, device1=14}) e
       * atribuindo-o à carga de mensagem do MQTT
       */
      Map<String, Integer> topK = SortTopK.sortTopK(
          scores,
          k,
          debugModeValue);

      this.waitReceiveScores();

      Map<String, Integer> topKReal = new LinkedHashMap<String, Integer>();

      for (String deviceId : topK.keySet()) {
        if (this.devicesScores.containsKey(deviceId)) {
          topKReal.put(deviceId, this.devicesScores.get(deviceId));
        }
      }

      if (k > scores.size()) {
        printlnDebug("Insufficient Top-K!");

        byte[] payload = String
            .format(
                "Can't possible calculate the Top-%s, sending the Top-%s!",
                k,
                scores.size())
            .getBytes();

        MQTTClientUp.publish("INVALID_TOP_K/" + id, payload, 1);
      }

      List<Map<String, Integer>> mapList = new ArrayList<>();
      mapList.add(topK);
      mapList.add(topKReal);

      byte[] payload = mapList.toString().getBytes();

      MQTTClientUp.publish("TOP_K_HEALTH_RES/" + id, payload, 1);

      this.devicesScores.clear();
    }
  }

  /**
   * Calcula o Top-k dos Top-ks de acordo com o valor de k solicitado.
   */
  public void calculateGeneralTopK(String id, int k) {
    printlnDebug("OK... now let's calculate the TOP-K of TOP-K's!");

    /*
     * Reordenando o mapa de Top-K (Ex: {device2=23, device1=14}) e
     * atribuindo-o à carga de mensagem do MQTT
     */
    Map<String, Integer> topK = SortTopK.sortTopK(
        this.getMapById(id),
        k,
        debugModeValue);

    this.waitReceiveScores();

    Map<String, Integer> topKReal = new LinkedHashMap<String, Integer>();

    for (String deviceId : topK.keySet()) {
      if (this.devicesScores.containsKey(deviceId)) {
        topKReal.put(deviceId, this.devicesScores.get(deviceId));
      }
    }

    this.devicesScores.clear();

    printlnDebug("==== Fog gateway -> Cloud gateway  ====");

    List<Map<String, Integer>> mapList = new ArrayList<>();
    mapList.add(topK);
    mapList.add(topKReal);

    byte[] payload = mapList.toString().getBytes();

    MQTTClientUp.publish("TOP_K_HEALTH_RES/" + id, payload, 1);

    this.removeRequest(id);
    this.removeSpecificResponse(id);

    this.showResponseTime();

    this.amountDevicesScoresReceived = 0;
  }

  private void waitReceiveScores() {
    printlnDebug("...Waiting for timeout to receive device scores...");

    long start = System.currentTimeMillis();
    long end = start + this.timeoutInSeconds * 1000;
    while (
      this.devicesScores.size() != this.node.getDevices().size() + this.amountDevicesScoresReceived &&
      System.currentTimeMillis() < end
    ) {
      printlnDebug(
          String.format("\rScores Received: [%s/%s] ",
              this.devicesScores.size(),
              this.node.getDevices().size() + this.amountDevicesScoresReceived));
    }
  }

  /**
   * Exibe o tempo de resposta de uma solicitação.
   */
  private void showResponseTime() {
    this.responseTime = System.currentTimeMillis() - this.responseTime;

    printlnDebug("+------------------------------+");
    printlnDebug(
        String.format("Tempo de resposta: %s ms", this.responseTime));
    printlnDebug("+------------------------------+");
  }

  public Node getNode() {
    return node;
  }

  public void setNode(Node node) {
    this.node = node;
  }

  /**
   * Adiciona um URI na lista de URIs.
   *
   * @param uri String - URI que deseja adicionar.
   */
  @Override
  public void addNodeUri(String uri) {
    if (!this.nodesUris.contains(uri)) {
      this.nodesUris.add(uri);
    }

    printlnDebug(String.format("URI: %s added in the nodesIps list.", uri));
    this.showNodesConnected();
  }

  /**
   * Remove uma URI na lista de URIs.
   *
   * @param uri String - URI que deseja remover.
   */
  @Override
  public void removeNodeUri(String uri) {
    int pos = this.findNodeUri(uri);

    if (pos != -1) {
      this.nodesUris.remove(pos);

      printlnDebug(
          String.format("URI: %s removed in the nodesIps list.", uri));

      this.showNodesConnected();
    } else {
      printlnDebug("Error, the desired node was not found.");
    }
  }

  /**
   * Retorna a posição de um URI na lista de URIs
   *
   * @param uri String - URI que deseja a posição.
   * @return int
   */
  private int findNodeUri(String uri) {
    for (int pos = 0; pos < this.nodesUris.size(); pos++) {
      if (this.nodesUris.get(pos).equals(uri)) {
        return pos;
      }
    }

    return -1;
  }

  /**
   * Retorna a lista de URIs dos nós conectados.
   *
   * @return List
   */
  @Override
  public List<String> getNodeUriList() {
    return this.nodesUris;
  }

  /**
   * Retorna a quantidade de nós conectados.
   *
   * @return String
   */
  @Override
  public int getNodes() {
    return this.nodesUris.size();
  }

  /**
   * Exibe a URI dos nós que estão conectados.
   */
  public void showNodesConnected() {
    printlnDebug("+---- Nodes URI Connected ----+");
    for (String nodeIp : this.getNodeUriList()) {
      printlnDebug("     " + nodeIp);
    }

    if (this.getNodeUriList().size() == 0) {
      printlnDebug("        empty");
    }
    printlnDebug("+----------------------------+");
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      logger.info(str);
    }
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

  public void setTopKScores(Map<String, Map<String, Integer>> topKScores) {
    this.topKScores = topKScores;
  }

  public MQTTClient getMQTTClientHost() {
    return this.MQTTClientHost;
  }

  public void setMQTTClientHost(MQTTClient mQTTClientHost) {
    this.MQTTClientHost = mQTTClientHost;
  }

  public List<String> getNodesUris() {
    return nodesUris;
  }

  public void setNodesUris(List<String> nodesUris) {
    this.nodesUris = nodesUris;
  }

  /**
   * Verifica se o gateway possui filhos.
   *
   * @return boolean
   */
  @Override
  public boolean hasNodes() {
    return hasNodes;
  }

  public void setHasNodes(boolean hasNodes) {
    this.hasNodes = hasNodes;
  }

  public void setTimeoutInSeconds(int timeoutInSeconds) {
    this.timeoutInSeconds = timeoutInSeconds;
  }

  public long getResponseTime() {
    return this.responseTime;
  }

  public void setResponseTime(long responseTime) {
    this.responseTime = responseTime;
  }

  /**
   * Retorna um JSON contendo os tipos de sensores disponíveis.
   *
   * @return JsonObject
   */
  @Override
  public JsonObject getSensorsTypesJSON() {
    return sensorsTypesJSON;
  }

  public Map<String, Integer> getDevicesScores() {
    return this.devicesScores;
  }

  public void addDeviceScore(String deviceId, int score) {
    this.devicesScores.put(deviceId, score);
  }

  public void putDevicesScoresAll(Map<String, Integer> devicesScores) {
    this.amountDevicesScoresReceived += devicesScores.size();
    this.devicesScores.putAll(devicesScores);
  }

  public void setJsonGetTopK(JsonObject jsonGetTopK) {
    this.jsonGetTopK = jsonGetTopK;
  }
}
