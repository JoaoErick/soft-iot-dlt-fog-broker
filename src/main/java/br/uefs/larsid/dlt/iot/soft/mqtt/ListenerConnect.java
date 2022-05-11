package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerConnect implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String CONN = "CONN";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;

  /**
   * Método Construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerConnect(
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

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");
    String ipDown = new String(message.getPayload());

    printlnDebug("==== Receive Connect Request ====");

    /* Verificar qual o tópico recebido. */
    switch (params[0]) {
      case CONN:
        this.controllerImpl.addNodeIp(ipDown);
        
        printlnDebug(String.format("IP: %s added in the nodesIps list.", ipDown));
        
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
