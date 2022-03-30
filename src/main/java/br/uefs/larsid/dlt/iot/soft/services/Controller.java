package br.uefs.larsid.dlt.iot.soft.services;

import java.util.List;
import java.util.Map;

public interface Controller {
  void calculateTopK(String id, int k);

  List<String> getScoresById(String id);

  boolean putScores(String id, List<String> score);

  Map<String, List<String>> getTopKScores();
}
