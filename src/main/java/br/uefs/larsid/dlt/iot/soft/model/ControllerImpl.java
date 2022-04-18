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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private String childs;
  private String urlAPI;
  private Map<String, Map<String, Integer>> topKScores = new HashMap<String, Map<String, Integer>>();
  private List<Device> devices;
  private Map<String, Integer> responseQueue = new HashMap<String, Integer>();

  public ControllerImpl() {}

  /**
   * Inicialização do Bundle.
   */
  public void start() {
    this.MQTTClientUp.connect();
    this.MQTTClientHost.connect();
    this.MQTTClientDown.connect();

    this.loadConnectedDevices(ClientIotService.getApiIot(urlAPI));

    if (Integer.parseInt(this.childs) > 0) {
      new Listener(this, MQTTClientHost, INVALID_TOP_K, QOS, debugModeValue);
      new Listener(this, MQTTClientHost, TOP_K_RES, QOS, debugModeValue);
    }

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

  public void updateValuesSensors() {
    for (Device d : this.devices) {
      d.getLastValueSensors();
    }
  }

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
   * Calcula o Top-K dos Top-Ks recebidos.
   */
  @Override
  public void calculateTopK(String id) {
    printlnDebug("Waiting for Gateway nodes to send their Top-K");

    /* Enquanto a quantidade de respostas da requisição for menor que o número 
    de nós filhos */
    while (this.responseQueue.get(id) < Integer.parseInt(this.childs)) {}

    printlnDebug("OK... now let's calculate the TOP-K of TOP-K's!");

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
    this.removeSpecificResponse(id);
  }

  /**
   *
   */
  @Override
  public Map<String, Map<String, Integer>> getTopKScores() {
    return this.topKScores;
  }

  /**
   * @param id
   */
  @Override
  public Map<String, Integer> getMapById(String id) {
    return this.topKScores.get(id);
  }

  /**
   * @param id
   * @param Map
   */
  @Override
  public boolean putScores(String id, Map<String, Integer> fogMap) {
    return this.topKScores.put(id, fogMap).isEmpty();
  }

  /**
   * @param mapAsString
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
   * @param topicId
   * @param message
   */
  @Override
  public void sendInvalidTopKMessage(String topicId, String message) {
    printlnDebug(message);

    MQTTClientUp.publish(INVALID_TOP_K_FOG + topicId, message.getBytes(), QOS);
  }

  /**
   *
   * @param id
   */
  @Override
  public void removeRequest(String id) {
    this.topKScores.remove(id);
  }

  /**
   * Cria uma nova chave no mapa de resposta dos filhos.
   *
   * @param key String - Id da requisição.
   */
  @Override
  public void addReponse(String key) {
    responseQueue.put(key, 0);

    // TODO: Remover.
    printlnDebug("QTD RESPOSTAS (vazio): " + responseQueue.get(key));
  }

  /**
   * Assim autaliza a quantidade de respostas.
   *
   * @param key String - Id da requisição.
   */
  @Override
  public void updateResponse(String key) {
    int temp = responseQueue.get(key);
    responseQueue.put(key, ++temp);

    // TODO: Remover.
    printlnDebug("QTD RESPOSTAS: " + responseQueue.get(key));
  }

  /**
   * Remove uma resposta específica da fila de respostas.
   *
   *@param key String - Id da requisição.
   */
  @Override
  public void removeSpecificResponse(String key) {
    responseQueue.remove(key);
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

  @Override
  public void sendEmptyTopK(String topicId) {
    byte[] payload = new HashMap<String, Map<String, Integer>>()
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
