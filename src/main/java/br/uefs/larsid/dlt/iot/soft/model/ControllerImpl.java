package br.uefs.larsid.dlt.iot.soft.model;

import br.uefs.larsid.dlt.iot.soft.entity.Device;
import br.uefs.larsid.dlt.iot.soft.entity.Sensor;
import br.uefs.larsid.dlt.iot.soft.mqtt.Listener;
import br.uefs.larsid.dlt.iot.soft.mqtt.ListenerTopK;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class ControllerImpl implements Controller {

  /*-------------------------Constantes---------------------------------------*/
  private static final int QOS = 1;
  private static final String TOP_K = "TOP_K_HEALTH_FOG/#";
  private static final String TOP_K_RES_FOG = "TOP_K_HEALTH_FOG_RES/";
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES/#";
  private static final String INVALID_TOP_K = "INVALID_TOP_K/#";
  private static final String INVALID_TOP_K_FOG = "INVALID_TOP_K_FOG/";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientHost;
  private MQTTClient MQTTClientDown;
  private String nodes;
  private String urlAPI;
  private Map<String, Map<String, Integer>> topKScores = new LinkedHashMap<String, Map<String, Integer>>();
  private List<Device> devices;
  private Map<String, Integer> responseQueue = new LinkedHashMap<String, Integer>();

  public ControllerImpl() {}

  /**
   * Inicializa o bundle.
   */
  public void start() {
    this.MQTTClientUp.connect();
    this.MQTTClientHost.connect();
    this.MQTTClientDown.connect();

    this.loadConnectedDevices(ClientIotService.getApiIot(this.urlAPI));

    if (Integer.parseInt(this.nodes) > 0) {
      new Listener(this, MQTTClientHost, INVALID_TOP_K, QOS, debugModeValue);
      new Listener(this, MQTTClientHost, TOP_K_RES, QOS, debugModeValue);
    }

    new ListenerTopK(
      this,
      MQTTClientUp,
      MQTTClientDown,
      TOP_K,
      QOS,
      debugModeValue
    );
  }

  /**
   * Finaliza o bundle.
   */
  public void stop() {
    this.MQTTClientHost.unsubscribe(INVALID_TOP_K);
    this.MQTTClientHost.unsubscribe(TOP_K_RES);
    this.MQTTClientUp.unsubscribe(TOP_K);

    this.MQTTClientHost.disconnect();
    this.MQTTClientUp.disconnect();
    this.MQTTClientDown.disconnect();
  }

  /**
   * Atualiza os valores dos sensores.
   */
  public void updateValuesSensors() {
    for (Device d : this.devices) {
      d.getLastValueSensors();
    }
  }

  /**
   * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
   */
  public void loadConnectedDevices(){
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
   * @param k int - Quantidade de scores requisitados.
   */
  @Override
  public void publishTopK(String id, int k) {
    printlnDebug("Waiting for Gateway nodes to send their Top-K");

    /* Enquanto a quantidade de respostas da requisição for menor que o número 
    de nós filhos */
    while (this.responseQueue.get(id) < Integer.parseInt(this.nodes)) {}

    printlnDebug("OK... now let's calculate the TOP-K of TOP-K's!");

    /* Reordenando o mapa de Top-K (Ex: {device2=23, device1=14}) e 
    atribuindo-o à carga de mensagem do MQTT */
    Map<String, Integer> topK = sortTopK(this.getMapById(id), k);

    printlnDebug("Top-K Result => " + topK.toString());
    printlnDebug("==== Fog gateway -> Fog UP gateway  ====");

    byte[] payload = topK.toString().getBytes();

    MQTTClientUp.publish(TOP_K_RES_FOG + id, payload, 1);

    this.removeRequest(id);
    this.removeSpecificResponse(id);
  }

  /**
   * Calcula o Top-K.
   *
   * @param devicesAndScoresMap Map - Mapa de Top-K
   * @param k int - Quantidade de scores requisitados.
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

    /* Caso a quantidade de dispositivos conectados seja menor que a 
    quantidade requisitada. */
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
   *  Retorna o mapa de requisições do sistema, composto pelo
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
   * @param id String - Id da requisição.
   * @param fogMap Map - Mapa de requisições.
   * @return boolean
   */
  @Override
  public boolean putScores(String id, Map<String, Integer> fogMap) {
    return this.topKScores.put(id, fogMap).isEmpty();
  }

  /**
   * Converte uma String em um Map.
   *
   * @param mapAsString String - String que deseja converter.
   * @return Map
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
  public void addReponse(String id) {
    responseQueue.put(id, 0);
  }

  /**
   * Assim autaliza a quantidade de respostas.
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
   *@param id String - Id da requisição.
   */
  @Override
  public void removeSpecificResponse(String id) {
    responseQueue.remove(id);
  }

  public String getNodes() {
    return nodes;
  }

  public void setNodes(String nodes) {
    this.nodes = nodes;
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

  @Override
  public void sendEmptyTopK(String topicId) {
    byte[] payload = new LinkedHashMap<String, Map<String, Integer>>()
      .toString()
      .getBytes();

    this.MQTTClientUp.publish(TOP_K_RES_FOG + topicId, payload, QOS);
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

  public MQTTClient getMQTTClientDown() {
    return MQTTClientDown;
  }

  public void setMQTTClientDown(MQTTClient mQTTClientDown) {
    MQTTClientDown = mQTTClientDown;
  }
}
