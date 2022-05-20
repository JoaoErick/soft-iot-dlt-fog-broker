package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ListenerTopK implements IMqttMessageListener {

  /*-------------------------Constantes---------------------------------------*/
  private static final String TOP_K_FOG = "TOP_K_HEALTH_FOG";
  private static final String TOP_K_RES = "TOP_K_HEALTH_RES/";
  private static final String TOP_K_FOG_RES = "TOP_K_HEALTH_FOG_RES/";
  private static final String INVALID_TOP_K = "INVALID_TOP_K/";
  private static final int QOS = 1;
  /*--------------------------------------------------------------------------*/

  private boolean debugModeValue;
  private MQTTClient MQTTClientUp;
  private List<String> nodesUris;
  private Controller controllerImpl;

  /**
   * Método construtor.
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientUp   MQTTClient - Cliente MQTT do gateway superior.
   * @param nodesUris   List<String> - Lista de URIs.
   * @param topic          String - Tópico que será ouvido
   * @param qos            int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerTopK(
    Controller controllerImpl,
    MQTTClient MQTTClientUp,
    List<String> nodesUris,
    String topic,
    int qos,
    boolean debugModeValue
  ) {
    this.MQTTClientUp = MQTTClientUp;
    this.nodesUris = nodesUris;
    this.controllerImpl = controllerImpl;
    this.debugModeValue = debugModeValue;

    this.MQTTClientUp.subscribe(qos, this, topic);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    /* params = [topic, id] */
    final String[] params = topic.split("/");

    final int k = Integer.valueOf(new String(message.getPayload()));

    if (k == 0) {
      if (controllerImpl.hasNodes()) {
        printlnDebug("Top-K = 0");

        this.controllerImpl.sendEmptyTopK(params[1]);
      }
    } else {
      switch (params[0]) {
        case TOP_K_FOG:
          if (controllerImpl.hasNodes()) {
            printlnDebug("==== Cloud gateway -> Fog gateway  ====");
            /* Criando uma nova chave, no mapa de requisições */
            this.controllerImpl.addResponse(params[1]);

            byte[] messageDown = message.getPayload();

            String topicDown = String.format("%s/%s", TOP_K_FOG, params[1]);

            this.publishToDown(topicDown, messageDown);

            Map<String, Integer> scoreMapEmpty = new LinkedHashMap<String, Integer>();

            this.controllerImpl.getTopKScores().put(params[1], scoreMapEmpty);

            /* Publicando para a camada superior o Top-K resultante. */
            this.controllerImpl.publishTopK(params[1], k);
          } else {
            printlnDebug("==== Fog gateway -> Bottom gateway  ====");
            printlnDebug("Calculating scores from devices...");

            Map<String, Integer> scores = new LinkedHashMap<String, Integer>();

            /*
             * Consumindo API Iot para resgatar os valores mais atualizados dos
             * dispositivos.
             */
            this.controllerImpl.loadConnectedDevices();

            /**
             * Se não houver nenhum dispositivo conectado.
             */
            if (this.controllerImpl.getDevices().isEmpty()) {
              printlnDebug("Sorry, there are no devices connected.");

              byte[] payload = scores.toString().getBytes();

              MQTTClientUp.publish(TOP_K_FOG_RES + params[1], payload, 1);
            } else {
              scores = this.controllerImpl.calculateScores();

              /*
               * Reordenando o mapa de Top-K (Ex: {device2=23, device1=14}) e
               * atribuindo-o à carga de mensagem do MQTT
               */
              Map<String, Integer> topK =
                this.controllerImpl.sortTopK(scores, k);

              if (k > scores.size()) {
                printlnDebug("Invalid Top-K!");

                byte[] payload = String
                  .format(
                    "Can't possible calculate the Top-%s, sending the Top-%s!",
                    k,
                    scores.size()
                  )
                  .getBytes();

                MQTTClientUp.publish(INVALID_TOP_K + params[1], payload, 1);
              }

              printlnDebug("TOP_K => " + topK.toString());
              printlnDebug("=========================================");

              byte[] payload = topK.toString().getBytes();

              MQTTClientUp.publish(TOP_K_RES + params[1], payload, 1);
            }
          }

          break;
      }
    }
  }

  /**
   * Publica a requisição de Top-K para os nós filhos.
   *
   * @param topicDown String - Tópico.
   * @param messageDown byte[] - Mensagem que será enviada.
   */
  private void publishToDown(String topicDown, byte[] messageDown) {
    String user = this.MQTTClientUp.getUserName();
    String password = this.MQTTClientUp.getPassword();

    for (String nodeUri : this.nodesUris) {
      String uri[] = nodeUri.split(":");

      MQTTClient MQTTClientDown = new MQTTClient(
        this.debugModeValue,
        uri[0],
        uri[1],
        user,
        password
      );

      //TODO: Executar a conexão dentro de um try-catch, para quando o nó não estiver mais conectado.
      //TODO: Se o nó não estiver conectado, removê-lo da lista.
      MQTTClientDown.connect();
      MQTTClientDown.publish(topicDown, messageDown, QOS);
      MQTTClientDown.disconnect();
    }
  }

  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }
}
