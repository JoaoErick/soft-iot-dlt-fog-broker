package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.MQTTClientService;
import java.util.Arrays;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import java.util.logging.Logger;

public class MQTTClient implements MqttCallbackExtended, MQTTClientService {

  /*-------------------------Constantes---------------------------------------*/
  private static final int QOS = 1;
  private static final String CONNECTED = "CONNECTED/1/1";
  /*--------------------------------------------------------------------------*/

  private String ip;
  private String port;
  private String userName;
  private String password;
  private String serverURI;
  private MqttClient mqttClient;
  private MqttConnectOptions mqttOptions;
  private boolean debugModeValue;

  public MQTTClient() {}

  public MQTTClient(
    boolean debugModeValue,
    String ip,
    String port,
    String userName,
    String password
  ) {
    this.debugModeValue = debugModeValue;
    this.ip = ip;
    this.port = port;
    this.userName = userName;
    this.password = password;

    this.start();
  }

  /**
   * Inicializa o client MQTT.
   */
  public void start() {
    printlnDebug("Starting MQTTClient...");
    
    this.serverURI = String.format("tcp://%s:%s", this.ip, this.port);

    this.mqttOptions = new MqttConnectOptions();
    this.mqttOptions.setMaxInflight(200);
    this.mqttOptions.setConnectionTimeout(3);
    this.mqttOptions.setKeepAliveInterval(10);
    this.mqttOptions.setAutomaticReconnect(true);
    this.mqttOptions.setCleanSession(false);

    this.mqttOptions.setUserName(this.userName);
    this.mqttOptions.setPassword(this.password.toCharArray());
  }

  /**
   * Finaliza o client MQTT.
   */
  public void stop() {
    printlnDebug("Finishing MQTTClient....");
  }

  /**
   * O cliente conecta-se a um servidor MQTT usando as opções especificadas.
   */
  @Override
  public void connect() {
    try {
      printlnDebug(
        "Trying to connect to the MQTT broker " + this.serverURI + "..."
      );

      this.mqttClient =
        new MqttClient(
          this.serverURI,
          String.format("cliente_java_%d", System.currentTimeMillis()),
          new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir"))
        );

      this.mqttClient.setCallback(this);
      this.mqttClient.connect(mqttOptions);
    } catch (MqttException ex) {
      printlnDebug(
        "Error connecting to MQTT broker " + this.serverURI + " - " + ex
      );
    }
  }

  /**
   * O cliente desconecta-se do servidor.
   */
  @Override
  public void disconnect() {
    if (mqttClient == null || !mqttClient.isConnected()) {
      return;
    }

    try {
      mqttClient.disconnect();
      mqttClient.close();

      printlnDebug("Disconnected from MQTT broker!");
    } catch (MqttException ex) {
      printlnDebug("Error disconnecting from MQTT broker - " + ex);
    }
  }

  /**
   * O cliente realiza a assinatura nos tópicos e na qualidade do serviço
   * passados por parâmetro.
   *
   * @param qos      int - Qualidade do serviço.
   * @param listener IMqttMessageListener - Retorno da chamada para lidar
   *                 com as mensagens recebidas.
   * @param topics   String... Tópicos em que o cliente deve assinar.
   *
   * @return IMqttToken - Mecanismo para rastrear o término de uma tarefa
   *         assíncrona.
   */
  @Override
  public IMqttToken subscribe(
    int qos,
    IMqttMessageListener listener,
    String... topics
  ) {
    if (mqttClient == null || topics.length == 0) {
      return null;
    }

    int[] qualityOfServices = new int[topics.length];
    IMqttMessageListener[] listeners = new IMqttMessageListener[topics.length];

    for (int i = 0; i < topics.length; i++) {
      qualityOfServices[i] = qos;
      listeners[i] = listener;
    }

    try {
      return mqttClient.subscribeWithResponse(
        topics,
        qualityOfServices,
        listeners
      );
    } catch (MqttException ex) {
      printlnDebug(
        String.format(
          "Error subscribing to topics %s - %s",
          Arrays.asList(topics),
          ex
        )
      );
      return null;
    }
  }

  /**
   * Cancela a assinatura do cliente em um ou mais tópicos.
   *
   * @param topics String... - Tópicos para cancelar a assinatura.
   */
  @Override
  public void unsubscribe(String... topics) {
    if (mqttClient == null || !mqttClient.isConnected() || topics.length == 0) {
      return;
    }

    try {
      mqttClient.unsubscribe(topics);
    } catch (MqttException ex) {
      printlnDebug(
        String.format(
          "Error unsubscribing from topic %s - %s",
          Arrays.asList(topics),
          ex
        )
      );
    }
  }

  /**
   * Repassa informações para a publicação de uma mensagem em um tópico
   * no servidor.
   *
   * @param topic String - Tópico em que será publicada a mensagem.
   * @param payload byte[] - Matriz de bytes da mensagem.
   * @param qos int - Qualidade do serviço.
   */
  @Override
  public void publish(String topic, byte[] payload, int qos) {
    publish(topic, payload, qos, false);
  }

  /**
   * Publica uma mensagem em um tópico no servidor e retorna a mensagem
   * aos assinantes assim que for entregue.
   *
   * @param topic String - Tópico em que será publicada a mensagem.
   * @param payload byte[] - Matriz de bytes da mensagem.
   * @param qos int - Qualidade do serviço.
   * @param retained boolean - Determina se a mensagem deve ou não
   * ser retida no servidor.
   */
  private synchronized void publish(
    String topic,
    byte[] payload,
    int qos,
    boolean retained
  ) {
    try {
      if (mqttClient.isConnected()) {
        mqttClient.publish(topic, payload, qos, retained);
        printlnDebug(
          String.format("Topic %s published. %dB", topic, payload.length)
        );
      } else {
        printlnDebug(
          "Client disconnected, could not publish topic " + topic
        );
      }
    } catch (MqttException ex) {
      printlnDebug("Error to publish " + topic + " - " + ex);
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
    this.printlnDebug(String.format("Lost connection to broker (%s). %s", this.ip, cause));
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {}

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {}

  @Override
  public void connectComplete(boolean reconnect, String serverURI) {
    byte[] payload = "bytes".getBytes();
    this.publish(CONNECTED, payload, QOS);

    this.printlnDebug(
        String.format(
          "%s to the MQTT broker!",
          reconnect ? "Reconnected!" : "Connected"
        )
      );
  }

  private void printlnDebug(String str) {
    Logger log = Logger.getLogger(MQTTClient.class.getName());

    if (debugModeValue) {
      log.info(str);
    }
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
