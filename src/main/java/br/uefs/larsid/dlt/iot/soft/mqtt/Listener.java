package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Listener implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES";
  private static final String INVALID_TOP_K = "INVALID_TOP_K";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;

  /**
   * Método Construtor
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public Listener(
    Controller controllerImpl,
    MQTTClient MQTTClientHost,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.controllerImpl = controllerImpl;
    this.MQTTClientHost = MQTTClientHost;
    this.debugModeValue = debugModeValue;

    this.MQTTClientHost.subscribe(qos, this, topic);
  }

  /**
   *
   */
  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");
    String messageContent = new String(message.getPayload());

    printlnDebug(messageContent);

    if (params[0].equals(TOP_K_RES)) {
      printlnDebug("==== Bottom gateway -> Fog gateway  ====");

      printlnDebug("ANTES DO IF");
      if (!messageContent.equals("{}")) {
        Map<String, Integer> bottomMap = controllerImpl.convertStrigToMap(
          messageContent
        );

        printlnDebug("DEPOIS DO IF");

        Map<String, Integer> fogMap = this.controllerImpl.getMapById(params[1]);

        fogMap.putAll(bottomMap);
        controllerImpl.putScores(params[1], fogMap);

        printlnDebug(
          "Top-K response received: " +
          controllerImpl.getMapById(params[1]).toString()
        );

        // Inicia o cálculo de Top-K
        controllerImpl.calculateTopK(params[1]);
      } else {
        printlnDebug("DENTRO DO ELSE");
        
        this.controllerImpl.sendEmptyTopK(params[1]);
        this.controllerImpl.removeRequest(params[1]);
      }
    } else if (params[0].equals(INVALID_TOP_K)) {
      printlnDebug("Invalid Top-K!");

      this.controllerImpl.sendInvalidTopKMessage(params[1], messageContent);
    }
  }

  /**
   *
   * @param str
   */
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
