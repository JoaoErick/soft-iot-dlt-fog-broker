package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.logging.Logger;

public class ListenerConnection implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String CONNECT = "SYN";
  private static final String DISCONNECT = "FIN";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;
  private static final Logger logger = Logger.getLogger(ListenerConnection.class.getName());

  /**
   * Método Construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientHost MQTTClient - Cliente MQTT do gateway inferior.
   * @param topics String[] - Tópicos que serão assinados.
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerConnection(
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
    String uriDown = new String(message.getPayload());

    printlnDebug("==== Receive Connect Request ====");

    /* Verificar qual o tópico recebido. */
    switch (params[0]) {
      case CONNECT:

        try {
          this.controllerImpl.addNodeUri(uriDown);
        } catch (Exception e) {
          printlnDebug(e.getMessage());
          printlnDebug("--");
          printlnDebug(e.toString());
          printlnDebug("--");
          printlnDebug(e.getStackTrace().toString());
        }

        break;
      case DISCONNECT:
        this.controllerImpl.removeNodeUri(uriDown);

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
