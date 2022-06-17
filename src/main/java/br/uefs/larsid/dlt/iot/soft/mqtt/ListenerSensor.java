package br.uefs.larsid.dlt.iot.soft.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerSensor implements IMqttMessageListener {

  private boolean debugModeValue;
  private MQTTClient MQTTClientHost;

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
}
