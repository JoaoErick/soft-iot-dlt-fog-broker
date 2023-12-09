package br.uefs.larsid.dlt.iot.soft.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import br.uefs.larsid.dlt.iot.soft.models.Node;

import java.util.List;
import java.util.Map;

public interface Controller {
  /**
   * Calcula o score dos dispositivos conectados.
   *
   * @return Map
   */
  Map<String, Integer> calculateScores(JsonArray functionHealth);

  /**
   * Calcula o Top-k dos dispositivos na névoa.
   */
  void calculateTopKUp();

  /**
   * Calcula o Top-k dos dispositivos na borda da rede.
   */
  void calculateTopKDown();

  /**
   * Publica o Top-K calculado para a camada de cima.
   *
   * @param id String - Id da requisição.
   * @param k int - Quantidade de scores requisitados.
   * @param functionHealth JsonArray - Array contendo a função de cálculo do
   * Top-K.
   */
  void publishTopK(String id, int k, JsonArray functionHealth);

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

  // /**
  //  * Retorna a quantidade de nós conectados.
  //  *
  //  * @return String
  //  */
  // List<Device> getDevices();

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

  // /**
  //  * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
  //  */
  // void loadConnectedDevices();

  public Node getNode();

  public void setNode(Node node);

  /**
   * Adiciona um URI na lista de URIs.
   *
   * @param uri String - URI que deseja adicionar.
   */
  public void addNodeUri(String uri);

  /**
   * Remove uma URI na lista de URIs.
   *
   * @param uri String - URI que deseja remover.
   */
  public void removeNodeUri(String uri);

  /**
   * Verifica se o gateway possui filhos.
   *
   * @return boolean
   */
  public boolean hasNodes();

  /**
   * Retorna a lista de URIs dos nós conectados.
   *
   * @return List
   */
  public List<String> getNodeUriList();

  /**
   * Exibe a URI dos nós que estão conectados.
   */
  public void showNodesConnected();

  /**
   * Publica os tipos de sensores para a camada de cima.
   */
  public void publishSensorType();

  /**
   * Adiciona os sensores em um JSON para enviar para a camada superior.
   *
   * @param jsonReceived JsonObject - JSON contendo os tipos dos sensores.
   */
  public void putSensorsTypes(JsonObject jsonReceived);

  /**
   * Retorna um JSON contendo os tipos de sensores disponíveis.
   *
   * @return JsonObject
   */
  public JsonObject getSensorsTypesJSON();

  /**
   * Requisita os tipos de sensores de um dispositivo conectado.
   *
   * @return List<String>
   */
  public List<String> loadSensorsTypes();

  public Map<String, Integer> getDevicesScores();

  public void addDeviceScore(String deviceId, int score);

  public void putDevicesScoresAll(Map<String, Integer> devicesScores);

  public void setJsonGetTopKDown(JsonObject jsonGetTopKDown);

  public long getResponseTime();

  public void setResponseTime(long responseTime);
}
