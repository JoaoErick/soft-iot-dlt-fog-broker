package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Listener implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_BOTTOM_RES = "TOP_K_HEALTH_RES";
  private static final String INVALID_TOP_K = "INVALID_TOP_K";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient mqttClientBottomBroker;

  /**
   * Método Construtor
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param mqttClientBottomBroker MQTTClient - Cliente MQTT do gateway inferior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public Listener(
    Controller controllerImpl,
    MQTTClient mqttClientBottomBroker,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.controllerImpl = controllerImpl;
    this.mqttClientBottomBroker = mqttClientBottomBroker;
    this.debugModeValue = debugModeValue;

    this.mqttClientBottomBroker.subscribe(qos, this, topic);
  }

  /**
   *
   */
  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");
    String messageContent = new String(message.getPayload());

    printlnDebug("==== Bottom gateway -> Fog gateway  ====");

    /* Verificar qual o tópico recebido. */
    switch (params[0]) {
      case TOP_K_BOTTOM_RES:
        /* Se o mapa de scores recebido for diferente de vazio. */
        if (!messageContent.equals("{}")) {
          Map<String, Integer> fogMap =
            this.controllerImpl.getMapById(params[1]);

          /* Adicionando o mapa de scores recebido no mapa geral, levando em 
          consideração o id da requisição. */
          fogMap.putAll(controllerImpl.convertStrigToMap(messageContent));
          controllerImpl.putScores(params[1], fogMap);

          printlnDebug(
            "Top-K response received: " +
            controllerImpl.getMapById(params[1]).toString()
          );

          /* Executando o cálculo de Top-K. */
          controllerImpl.calculateTopK(params[1]);
        } else {
          this.controllerImpl.sendEmptyTopK(params[1]);
          this.controllerImpl.removeRequest(params[1]);
        }
        break;
      case INVALID_TOP_K:
        printlnDebug("Invalid Top-K!");

        this.controllerImpl.sendInvalidTopKMessage(params[1], messageContent);
        break;
    }
  }

  /**
   *
   * @param str
   */
  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }
}
