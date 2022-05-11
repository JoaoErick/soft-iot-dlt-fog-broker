package br.uefs.larsid.dlt.iot.soft.services;

import br.uefs.larsid.dlt.iot.soft.entity.Device;
import java.util.List;
import java.util.Map;

public interface Controller {
  /**
   * Calcula o score dos dispositivos conectados.
   *
   * @return Map
   */
  Map<String, Integer> calculateScores();

  /**
   * Calcula o Top-K.
   *
   * @param devicesAndScoresMap Map - Mapa de Top-K
   * @param k int - Quantidade de scores requisitados.
   * @return Map
   *
   */
  Map<String, Integer> sortTopK(
    Map<String, Integer> devicesAndScoresMap,
    int k
  );

  /**
   * Publica o Top-K calculado para a camada de cima.
   *
   * @param id String - Id da requisição.
   * @param k int - Quantidade de scores requisitados.
   */
  void publishTopK(String id, int k);

  /**
   * Retorna o mapa de scores de acordo com o id da requisição
   * passado por parâmetro.
   *
   * @param id String - Id da requisição.
   * @return Map
   */
  Map<String, Integer> getMapById(String id);

  /**
   * Adiciona um mapa de scores de uma nova requisição no mapa de
   * requisições na sua respectiva.
   *
   * @param id String - Id da requisição.
   * @param fogMap Map - Mapa de requisições.
   */
  void putScores(String id, Map<String, Integer> fogMap);

  /**
   *  Retorna o mapa de requisições do sistema, composto pelo
   * id da requisição (chave) e o mapa de scores (valor).
   * O mapa de scores é composto pelo nome do dispositivo (Chave)
   * e o score (valor) associado.
   *
   * @return Map
   */
  Map<String, Map<String, Integer>> getTopKScores();

  /**
   * Converte uma String em um Map.
   *
   * @param mapAsString String - String que deseja converter.
   * @return Map
   */
  Map<String, Integer> convertStringToMap(String mapAsString);

  /**
   * Envia um mapa vazio.
   *
   * @param topicId String - Id da requisição.
   */
  void sendEmptyTopK(String topicId);

  /**
   * Envia uma mensagem indicando que o Top-K pedido possui uma quantidade
   * inválida.
   *
   * @param topicId String - Id da requisição do Top-K.
   * @param message String - Mensagem.
   */
  void sendInvalidTopKMessage(String topicId, String message);

  /**
   * Remove do mapa de requisições o id da requisição junto com mapa de scores
   * associado a ele.
   *
   * @param id String - Id da requisição.
   */
  void removeRequest(String id);

  /**
   * Retorna a quantidade de nós conectados.
   *
   * @return String
   */
  int getNodes();

  /**
   * Retorna a quantidade de nós conectados.
   *
   * @return String
   */
  List<Device> getDevices();

  /**
   * Cria uma nova chave no mapa de resposta dos filhos.
   *
   * @param id String - Id da requisição.
   */
  void addResponse(String key);

  /**
   * Atualiza a quantidade de respostas.
   *
   * @param id String - Id da requisição.
   */
  void updateResponse(String key);

  /**
   * Remove uma resposta específica da fila de respostas.
   *
   *@param id String - Id da requisição.
   */
  void removeSpecificResponse(String key);

  /**
   * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
   */
  void loadConnectedDevices();

  /**
   * Adiciona um IP na lista de IPs.
   *
   * @param ip String - Ip que deseja adicionar.
   */
  public void addNodeIp(String ip);

  /**
   * Remove um IP na lista de IPs.
   *
   * @param ip String - Ip que deseja remover.
   */
  public void removeNodeIp(String ip);
}
