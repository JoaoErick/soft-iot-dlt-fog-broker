package br.uefs.larsid.dlt.iot.soft.utils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SortTopK {

  /**
   * Ordena o mapa contendo os scores dos dispositivos.
   *
   * @param map Map<String, Integer> - Mapa com os valores para serem ordenados.
   * @param k int - Valor do K do Top-K
   * @param debugModeValue boolean - Modo de depuração
   * @return Map<String, Integer>
   */
  public static Map<String, Integer> sortTopK(
    Map<String, Integer> map,
    int k,
    boolean debugModeValue
  ) {
    Object[] temp = map
      .entrySet()
      .stream()
      .sorted(
        Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
      )
      .toArray();

    Map<String, Integer> topK = new LinkedHashMap<String, Integer>();

    /*
     * Caso a quantidade de dispositivos conectados seja menor que a
     * quantidade requisitada.
     */
    int maxIteration = k <= map.size() ? k : map.size();

    /* Pegando os k piores */
    for (int i = 0; i < maxIteration; i++) {
      Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) temp[i];
      topK.put(e.getKey(), e.getValue());
    }

    return topK;
  }
}
