package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.List;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Listener implements IMqttMessageListener {

  private static final String TOP_K_RES = "TOP_K_HEALTH_RES";

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTClientHost;

  public Listener(
    Controller controllerImpl,
    MQTTClient MQTTClientHost,
    String topic,
    int qos
  ) {
    this.controllerImpl = controllerImpl;
    this.MQTTClientHost = MQTTClientHost;

    this.MQTTClientHost.subscribe(qos, this, topic);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");

    printlnDebug("==== Bottom gateway -> Fog gateway  ====");

    String messageContent = new String(message.getPayload());

    if (params.equals(TOP_K_RES)) {
      List<String> score = this.controllerImpl.getScoresById(params[1]);

      score.add(messageContent);
      controllerImpl.putScores(params[1], score);

      printlnDebug(
        "Top-k response received: " +
        controllerImpl.getScoresById(params[1]).toString()
      );
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
