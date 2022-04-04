package br.uefs.larsid.dlt.iot.soft.services;

import java.util.Map;

public interface Controller {
  void calculateTopK(String id, int k);

  Map<String, Integer> getMapById(String id);

  boolean putScores(String id, Map<String, Integer> fogMap);

  Map<String, Map<String, Integer>> getTopKScores();

  Map<String, Integer> convertStrigToMap(String mapAsString);

  void sendInvalidTopKMessage(String topicId, String message);
}
