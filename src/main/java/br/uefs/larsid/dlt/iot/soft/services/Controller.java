package br.uefs.larsid.dlt.iot.soft.services;

import br.uefs.larsid.dlt.iot.soft.entity.Device;
import java.util.List;
import java.util.Map;

public interface Controller {
  Map<String, Integer> calculateScores();

  Map<String, Integer> sortTopK(
    Map<String, Integer> devicesAndScoresMap,
    int k
  );

  void publishTopK(String id, int k);

  Map<String, Integer> getMapById(String id);

  boolean putScores(String id, Map<String, Integer> fogMap);

  Map<String, Map<String, Integer>> getTopKScores();

  Map<String, Integer> convertStrigToMap(String mapAsString);

  void sendEmptyTopK(String topicId);

  void sendInvalidTopKMessage(String topicId, String message);

  void removeRequest(String id);

  String getNodes();

  void updateValuesSensors();

  List<Device> getDevices();

  void addReponse(String key);

  void updateResponse(String key);

  void removeSpecificResponse(String key);

  void loadConnectedDevices();
}
