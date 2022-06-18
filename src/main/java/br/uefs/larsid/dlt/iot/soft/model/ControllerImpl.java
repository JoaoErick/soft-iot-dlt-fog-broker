package br.uefs.larsid.dlt.iot.soft.model;

import br.uefs.larsid.dlt.iot.soft.entity.Device;
import br.uefs.larsid.dlt.iot.soft.entity.Sensor;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerConnection;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerRequest;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerResponse;
import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;
import br.uefs.larsid.dlt.iot.soft.services.Controller;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;

public class ControllerImpl implements Controller {

  /*-------------------------Constantes---------------------------------------*/
  private static final int QOS = 1;
  private static final String TOP_K_FOG = "TOP_K_HEALTH_FOG/#";
  private static final String TOP_K = "TOP_K_HEALTH/#";
  private static final String SENSORS_FOG = "SENSORS_FOG/";
  private static final String SENSORS = "SENSORS/";
  private static final String TOP_K_RES_FOG = "TOP_K_HEALTH_FOG_RES/";
  private static final String SENSORS_FOG_RES = "SENSORS_FOG_RES/";
  private static final String SENSORS_RES = "SENSORS_RES/";
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES/#";
  private static final String INVALID_TOP_K = "INVALID_TOP_K/#";
  private static final String INVALID_TOP_K_FOG = "INVALID_TOP_K_FOG/";
  private static final String CONNECT = "SYN";
  private static final String DISCONNECT = "FIN";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private boolean hasNodes;
  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientHost;
  private String urlAPI;
  private Map<String, Map<String, Integer>> topKScores = new LinkedHashMap<String, Map<String, Integer>>();
  private List<Device> devices;
  private Map<String, Integer> responseQueue = new LinkedHashMap<String, Integer>();
  private List<String> nodesUris;
  private int timeoutInSeconds;
  private JSONObject sensorsTypesJSON = new JSONObject();

  public ControllerImpl() {}

  /**
   * Inicializa o bundle.
   */
  public void start() {
    this.MQTTClientUp.connect();
    this.MQTTClientHost.connect();

    if (hasNodes) {
      nodesUris = new ArrayList<>();
      String[] topicsRequest = { TOP_K_FOG, SENSORS_FOG };
      String[] topicsConnection = { CONNECT, DISCONNECT };
      String[] topicsResponse = { TOP_K_RES, INVALID_TOP_K, SENSORS_RES };

      new ListenerRequest(
        this,
        MQTTClientUp,
        MQTTClientHost,
        this.nodesUris,
        topicsRequest,
        QOS,
        debugModeValue
      );
      new ListenerConnection(
        this,
        MQTTClientHost,
        topicsConnection,
        QOS,
        debugModeValue
      );
      new ListenerResponse(
        this,
        MQTTClientHost,
        topicsResponse,
        QOS,
        debugModeValue
      );
    } else {
      String[] topics = { TOP_K, SENSORS };

      new ListenerRequest(
        this,
        MQTTClientUp,
        MQTTClientHost,
        this.nodesUris,
        topics,
        QOS,
        debugModeValue
      );

      byte[] payload = String
        .format("%s:%s", MQTTClientHost.getIp(), MQTTClientHost.getPort())
        .getBytes();

      this.MQTTClientUp.publish(CONNECT, payload, QOS);
    }
  }

  /**
   * Finaliza o bundle.
   */
  public void stop() {
    if (!this.hasNodes) {
      byte[] payload = String
        .format("%s:%s", MQTTClientHost.getIp(), MQTTClientHost.getPort())
        .getBytes();

      this.MQTTClientUp.publish(DISCONNECT, payload, QOS);

      this.MQTTClientUp.unsubscribe(TOP_K);
      this.MQTTClientUp.unsubscribe(SENSORS);
    } else {
      this.MQTTClientUp.unsubscribe(TOP_K_FOG);
      this.MQTTClientUp.unsubscribe(SENSORS_FOG);
      this.MQTTClientUp.unsubscribe(CONNECT);
      this.MQTTClientUp.unsubscribe(DISCONNECT);
      this.MQTTClientHost.unsubscribe(TOP_K_RES);
      this.MQTTClientHost.unsubscribe(INVALID_TOP_K);
      this.MQTTClientHost.unsubscribe(SENSORS_RES);
    }

    this.MQTTClientHost.disconnect();
    this.MQTTClientUp.disconnect();
  }

  /**
   * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
   */
  @Override
  public void loadConnectedDevices() {
    this.loadConnectedDevices(ClientIotService.getApiIot(this.urlAPI));
  }

  /**
   * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
   *
   * @param strDevices String - Dispositivos requisitados.
   */
  private void loadConnectedDevices(String strDevices) {
    List<Device> devicesTemp = new ArrayList<Device>();

    try {
      printlnDebug("JSON load:");
      printlnDebug(strDevices);

      JSONArray jsonArrayDevices = new JSONArray(strDevices);

      for (int i = 0; i < jsonArrayDevices.length(); i++) {
        JSONObject jsonDevice = jsonArrayDevices.getJSONObject(i);
        ObjectMapper mapper = new ObjectMapper();
        Device device = mapper.readValue(jsonDevice.toString(), Device.class);

        devicesTemp.add(device);

        List<Sensor> tempSensors = new ArrayList<Sensor>();
        JSONArray jsonArraySensors = jsonDevice.getJSONArray("sensors");

        for (int j = 0; j < jsonArraySensors.length(); j++) {
          JSONObject jsonSensor = jsonArraySensors.getJSONObject(j);
          Sensor sensor = mapper.readValue(jsonSensor.toString(), Sensor.class);
          sensor.setUrlAPI(urlAPI);
          tempSensors.add(sensor);
        }

        device.setSensors(tempSensors);
      }
    } catch (JsonParseException e) {
      e.printStackTrace();
      System.out.println(
        "Verify the correct format of 'DevicesConnected' property in configuration file."
      );
    } catch (JsonMappingException e) {
      e.printStackTrace();
      System.out.println(
        "Verify the correct format of 'DevicesConnected' property in configuration file."
      );
    } catch (IOException e) {
      e.printStackTrace();
    }

    this.devices = devicesTemp;

    printlnDebug("Amount of devices connected: " + this.devices.size());
  }

  /**
   * Calcula o score dos dispositivos conectados.
   *
   * @return Map
   */
  @Override
  public Map<String, Integer> calculateScores() {
    Map<String, Integer> temp = new LinkedHashMap<String, Integer>();

    for (Device device : this.devices) {
      // TODO: Implementar função para cálculo do score.
      Random random = new Random();
      int score = random.nextInt(51);

      temp.put(device.getId(), score);
    }

    return temp;
  }

  /**
   * Publica o Top-K calculado para a camada de cima.
   *
   * @param id String - Id da requisição.
   * @param k  int - Quantidade de scores requisitados.
   */
  @Override
  public void publishTopK(String id, int k) {
    printlnDebug("Waiting for Gateway nodes to send their Top-K");

    long start = System.currentTimeMillis();
    long end = start + this.timeoutInSeconds * 1000;

    /*
     * Enquanto a quantidade de respostas da requisição for menor que o número
     * de nós filhos
     */
    while (
      this.responseQueue.get(id) < this.nodesUris.size() &&
      System.currentTimeMillis() < end
    ) {}

    /*
     * Consumindo apiIot para pegar os valores mais atualizados dos
     * dispositivos.
     */
    this.loadConnectedDevices();

    if (!this.devices.isEmpty()) {
      /* Adicionando os dispositivos conectados em si mesmo. */
      this.putScores(id, this.calculateScores());

      int topkMapSize = this.topKScores.get(id).size();

      if (topkMapSize < k) {
        printlnDebug("Invalid Top-K!");

        this.sendInvalidTopKMessage(
            id,
            String.format(
              "Can't possible calculate the Top-%s, sending the Top-%d!",
              k,
              topkMapSize
            )
          );
      }
    }

    printlnDebug("OK... now let's calculate the TOP-K of TOP-K's!");

    /*
     * Reordenando o mapa de Top-K (Ex: {device2=23, device1=14}) e
     * atribuindo-o à carga de mensagem do MQTT
     */
    Map<String, Integer> topK = sortTopK(this.getMapById(id), k);

    printlnDebug("Top-K Result => " + topK.toString());
    printlnDebug("==== Fog gateway -> Cloud gateway  ====");

    byte[] payload = topK.toString().getBytes();

    MQTTClientUp.publish(TOP_K_RES_FOG + id, payload, 1);

    this.removeRequest(id);
    this.removeSpecificResponse(id);
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
    while (
      this.responseQueue.get("getSensors") < this.nodesUris.size() &&
      System.currentTimeMillis() < end
    ) {}

    byte[] payload = sensorsTypesJSON.toString().getBytes();

    MQTTClientUp.publish(SENSORS_FOG_RES, payload, 1);

    this.removeSpecificResponse("getSensors");
  }

  /**
   * Calcula o Top-K.
   *
   * @param devicesAndScoresMap Map - Mapa de Top-K
   * @param k                   int - Quantidade de scores requisitados.
   * @return Map
   *
   */
  @Override
  public Map<String, Integer> sortTopK(
    Map<String, Integer> devicesAndScoresMap,
    int k
  ) {
    Object[] temp = devicesAndScoresMap
      .entrySet()
      .stream()
      .sorted(
        Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
      )
      .toArray();

    if (debugModeValue) {
      for (Object e : temp) {
        printlnDebug(
          ((Map.Entry<String, Integer>) e).getKey() +
          " : " +
          ((Map.Entry<String, Integer>) e).getValue()
        );
      }
    }

    Map<String, Integer> topK = new LinkedHashMap<String, Integer>();

    /*
     * Caso a quantidade de dispositivos conectados seja menor que a
     * quantidade requisitada.
     */
    int maxIteration = k <= devicesAndScoresMap.size()
      ? k
      : devicesAndScoresMap.size();

    /* Pegando os k piores */
    for (int i = 0; i < maxIteration; i++) {
      Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) temp[i];
      topK.put(e.getKey(), e.getValue());
    }

    return topK;
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
  public void putSensorsTypes(JSONObject jsonReceived) {
    if (sensorsTypesJSON.getJSONArray("sensors").length() == 0) {
      sensorsTypesJSON = jsonReceived;
    } else {
      JSONArray sensorsTypesJSONArray = sensorsTypesJSON.getJSONArray(
        "sensors"
      );
      JSONArray receivedJSONArray = jsonReceived.getJSONArray("sensors");

      if (
        !sensorsTypesJSONArray
          .toString()
          .equals(receivedJSONArray.toString()) ||
        receivedJSONArray.length() > 0
      ) {
        String[] sensorsTypesArray = new String[sensorsTypesJSONArray.length()];
        String[] receivedArray = new String[receivedJSONArray.length()];

        for (int i = 0; i < sensorsTypesJSONArray.length(); i++) {
          sensorsTypesArray[i] = sensorsTypesJSONArray.getString(i);
        }

        for (int i = 0; i < receivedJSONArray.length(); i++) {
          receivedArray[i] = receivedJSONArray.getString(i);
        }

        String[] result = Stream
          .concat( // combine
            Stream.of(sensorsTypesArray),
            Stream.of(receivedArray)
          )
          .distinct() // filter duplicates
          .sorted() // sort
          .toArray(String[]::new);

        sensorsTypesJSON.put("sensors", new JSONArray(result));
      }
    }
  }

  /**
   * Converte uma String em um Map.
   *
   * @param mapAsString String - String que deseja converter.
   * @return Map
   */
  @Override
  public Map<String, Integer> convertStringToMap(String mapAsString) {
    return Arrays
      .stream(mapAsString.substring(1, mapAsString.length() - 1).split(", "))
      .map(entry -> entry.split("="))
      .collect(
        Collectors.toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1]))
      );
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

      printlnDebug(String.format("URI: %s removed in the nodesIps list.", uri));

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
      System.out.println(str);
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

  public String getUrlAPI() {
    return urlAPI;
  }

  public void setUrlAPI(String urlAPI) {
    this.urlAPI = urlAPI;
  }

  public List<Device> getDevices() {
    return devices;
  }

  public void setDevices(List<Device> devices) {
    this.devices = devices;
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

  /**
   * Retorna um JSON contendo os tipos de sensores disponíveis.
   *
   * @return JSONObject
   */
  @Override
  public JSONObject getSensorsTypesJSON() {
    return sensorsTypesJSON;
  }
}
