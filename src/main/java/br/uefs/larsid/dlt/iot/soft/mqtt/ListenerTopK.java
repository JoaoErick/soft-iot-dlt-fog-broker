package br.uefs.larsid.dlt.iot.soft.mqtt;

import br.uefs.larsid.dlt.iot.soft.entity.Device;
import br.uefs.larsid.dlt.iot.soft.services.Controller;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
  private MQTTClient MQTTClientDown;
  private Controller controllerImpl;

  /**
   *
   * @param controllerImpl Controller - Controller que fará uso desse Listener.
   * @param MQTTClientUp MQTTClient - Cliente MQTT do gateway superior.
   * @param MQTTClientDown MQTTClient - Cliente MQTT do gateway inferior.
   * @param topic String - Tópico que será ouvido
   * @param qos int - Qualidade de serviço do tópico que será ouvido.
   * @param debugModeValue boolean - Modo para debugar o código.
   */
  public ListenerTopK(
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
    this.debugModeValue = debugModeValue;
    this.MQTTClientUp.subscribe(qos, this, topic);
  }

  /**
   *
   */
  @Override
  public void messageArrived(String topic, MqttMessage message)
    throws Exception {
    final String[] params = topic.split("/");

    final int k = Integer.valueOf(params[2]);

    printlnDebug("==== Fog UP gateway -> Fog gateway  ====");

    printlnDebug("Request received: " + topic);

    if (k == 0) {
      printlnDebug("Top-K = 0");

      this.controllerImpl.sendEmptyTopK(params[1]);
    } else {
      switch (params[0]) {
        case TOP_K_FOG:
          if (Integer.parseInt(controllerImpl.getChilds()) > 0) {
            /* Criando uma nova chave, no mapa de requisições */
            this.controllerImpl.addReponse(params[1]);

            byte[] messageEmpty = "".getBytes();

            String topicDown = String.format(
              "%s/%s/%s",
              TOP_K_FOG,
              params[1],
              params[2]
            );

            MQTTClientDown.publish(topicDown, messageEmpty, QOS);

            Map<String, Integer> scoreMapEmpty = new HashMap<String, Integer>();

            controllerImpl.getTopKScores().put(params[1], scoreMapEmpty);

            /* Executando o cálculo de Top-K. */
            controllerImpl.calculateTopK(params[1], Integer.parseInt(params[2]));
          } else {
            Map<String, Integer> scores = new HashMap<String, Integer>();

            printlnDebug("Calculating scores from devices...");

            /* Consumindo apiIot para pegar os valores mais atualizados dos 
            .dispositivos */
            controllerImpl.updateValuesSensors();

            if (controllerImpl.getDevices().isEmpty()) {
              printlnDebug("Sorry, there are no devices connected.");

              byte[] payload = scores.toString().getBytes();
              // TODO Verificar se é realmente esse tópico.
              MQTTClientUp.publish(TOP_K_FOG_RES + params[1], payload, 1);
            } else {
              for (Device device : controllerImpl.getDevices()) {
                // TODO: Implementar função para cálculo do score.
                Random random = new Random();
                int score = random.nextInt(51);

                scores.put(device.getId(), score);
              }

              /* Ordenando o Map de Scores e colocando-o em um array. */
              Object[] temp = scores
                .entrySet()
                .stream()
                .sorted(
                  Map.Entry.<String, Integer>comparingByValue(
                    Comparator.reverseOrder()
                  )
                )
                .toArray();

              if (debugModeValue) {
                for (Object e : temp) {
                  printlnDebug(
                    ((Map.Entry<String, Integer>) e).getKey() +
                    " : " +
                    ((Map.Entry<String, Integer>) e).getValue()
                  );
                }
              }

              Map<String, Integer> topK = new HashMap<String, Integer>();

              /* Caso a quantidade de dispositivos conectados seja menor que a 
              quantidade requisitada. */
              int maxIteration = k <= scores.size() ? k : scores.size();

              /* Pegando os k piores */
              for (int i = 0; i < maxIteration; i++) {
                Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) temp[i];
                topK.put(e.getKey(), e.getValue());
              }

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
   *
   * @param str
   */
  private void printlnDebug(String str) {
    if (debugModeValue) {
      System.out.println(str);
    }
  }
}
