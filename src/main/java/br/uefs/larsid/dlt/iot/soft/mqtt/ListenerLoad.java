package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerLoad implements IMqttMessageListener {

  private MQTTClient MQTTClientUp;
  private MQTTClient MQTTClientDown;
  private Controller controllerImpl;

  /**
   * Método construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientUp MQTTClient - Cliente MQTT do gateway superior.
   * @param MQTTClientDown MQTTClient - Cliente MQTT do gateway inferior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerLoad(
    Controller controllerImpl,
    MQTTClient MQTTClientUp,
    MQTTClient MQTTClientDown,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.MQTTClientUp = MQTTClientUp;
    this.MQTTClientDown = MQTTClientDown;
    this.controllerImpl = controllerImpl;

    this.MQTTClientUp.subscribe(qos, this, topic);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    controllerImpl.loadConnectedDevices();

  }
}
