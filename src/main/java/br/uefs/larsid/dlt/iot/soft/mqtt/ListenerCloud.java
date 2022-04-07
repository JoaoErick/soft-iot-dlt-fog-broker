package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerCloud implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES";
  private static final String INVALID_TOP_K = "INVALID_TOP_K";
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private Controller controllerImpl;
  private MQTTClient MQTTCloud;
  private MQTTClient MQTTHost;
  private MQTTClient MQTTDown;

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    // TODO Auto-generated method stub

  }
}
