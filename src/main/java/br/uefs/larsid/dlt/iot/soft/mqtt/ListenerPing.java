package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerPing implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String PING = "PING";
  private static final String PING_RES = "PING_RES";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClient;
  private int qos;

  /**
   * Método Construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClient MQTTClient - Cliente MQTT do gateway superior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerPing(
    Controller controllerImpl,
    MQTTClient MQTTClient,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.controllerImpl = controllerImpl;
    this.MQTTClient = MQTTClient;
    this.debugModeValue = debugModeValue;
    this.qos = qos;

    //TODO: Receber tanto o MQTTClientHost quanto o MQTTClienUp

    this.MQTTClient.subscribe(qos, this, topic);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    printlnDebug("==== Receive Ping Request ====");

    /* Verificar qual o tópico recebido. */
    switch (topic) {
      case PING:
      printlnDebug("PING RECEBIDO!");
        this.MQTTClient.publish(PING_RES, "".getBytes(), this.qos);

        break;
      case PING_RES:
        printlnDebug("RESPOSTA RECEBIDA COM SUCESSO!");

        break;
    }
  }

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
