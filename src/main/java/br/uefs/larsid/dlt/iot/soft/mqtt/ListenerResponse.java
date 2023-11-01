package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import br.uefs.larsid.dlt.iot.soft.utils.ConvertStringToMap;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class ListenerResponse implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES";
  private static final String INVALID_TOP_K = "INVALID_TOP_K";
  private static final String SENSORS_RES = "SENSORS_RES";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;
  private static final Logger logger = Logger.getLogger(ListenerResponse.class.getName());

  /**
   * Método Construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topics String[] - Tópicos que serão assinados.
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerResponse(
    Controller controllerImpl,
    MQTTClient MQTTClientHost,
    String[] topics,
    int qos,
    boolean debugModeValue
  ) {
    this.controllerImpl = controllerImpl;
    this.MQTTClientHost = MQTTClientHost;
    this.debugModeValue = debugModeValue;

    for (String topic : topics) {
      this.MQTTClientHost.subscribe(qos, this, topic);
    }
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");
    String messageContent = new String(message.getPayload());

    printlnDebug("==== Bottom gateway -> Fog gateway  ====");

    /* Verificar qual o tópico recebido. */
    switch (params[0]) {
      case TOP_K_RES:
        Gson gson = new Gson();
        String scoreRealMap, scoreMap;

        List<Map<String, Integer>> list = gson.fromJson(messageContent, new TypeToken<List<Map<String, Integer>>>() {}.getType());

        scoreMap = list.get(0).toString();
        scoreRealMap = list.get(1).toString();

        if (!scoreRealMap.equals("{}")) {
          this.controllerImpl.putDevicesScoresAll(ConvertStringToMap.convertStringToMap(scoreRealMap));
        }

        /* Se o mapa de scores recebido for diferente de vazio. */
        if (!scoreMap.equals("{}")) {
          Map<String, Integer> fogMap =
            this.controllerImpl.getMapById(params[1]);

          /* Adicionando o mapa de scores recebido no mapa geral, levando em 
          consideração o id da requisição. */
          fogMap.putAll(ConvertStringToMap.convertStringToMap(scoreMap));
          controllerImpl.putScores(params[1], fogMap);

          printlnDebug(
            "Top-K response received and add to the map: " +
            controllerImpl.getMapById(params[1]).toString()
          );

          /* Adicionando nova requisição. */
          this.controllerImpl.updateResponse(params[1]);
        } else {
          // TODO: Testar com um dos filhos com mapa vazio.
          this.controllerImpl.sendEmptyTopK(params[1]);
          this.controllerImpl.removeRequest(params[1]);
        }

        break;
      case INVALID_TOP_K:
        printlnDebug("Insufficient Top-K! - " + messageContent);
        break;
      case SENSORS_RES:
        JsonObject jsonResponse = new Gson().fromJson(messageContent, JsonObject.class);

        this.controllerImpl.putSensorsTypes(jsonResponse);

        printlnDebug("Sensors response received and add to the map");

        /* Adicionando nova requisição. */
        this.controllerImpl.updateResponse("getSensors");

        break;
    }
  }

  private void printlnDebug(String str) {
    if (isDebugModeValue()) {
      logger.info(str);
    }
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }
}
