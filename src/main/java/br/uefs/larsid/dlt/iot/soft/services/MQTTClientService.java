package br.uefs.larsid.dlt.iot.soft.services;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public interface MQTTClientService {
  void connect();

  void disconnect();

  IMqttToken subscribe(
    int qos,
    IMqttMessageListener listener,
    String... topics
  );

  void unsubscribe(String... topics);

  void publish(String topic, byte[] payload, int qos);
}
