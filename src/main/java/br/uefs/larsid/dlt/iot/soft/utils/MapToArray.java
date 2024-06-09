package br.uefs.larsid.dlt.iot.soft.utils;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapToArray {

  /**
   * Converte um Map<String, Integer> em um Array de JsonObject.
   *
   * @param map Map<String, Integer> - Mapa que deseja converter
   * @return Object[]
   */
  @SuppressWarnings("unchecked")
  public static Object[] mapToArray(Map<String, Integer> map) {
    List<JsonObject> array = new ArrayList<JsonObject>();

    for (Object object : map.entrySet().stream().toArray()) {
      JsonObject json = new JsonObject();

      json.addProperty(
        "deviceId",
        ((Map.Entry<String, Integer>) object).getKey()
      );
      json.addProperty(
        "score",
        ((Map.Entry<String, Integer>) object).getValue()
      );

      array.add(json);
    }

    return array.toArray();
  }

  /**
   * Converte Map<String, Integer> em Array de JsonObject.
   *
   * @param map1 Map<String, Integer> - Mapa com os scores calculados no 
   * gateway que deseja converter
   * @param map2 Map<String, Integer> - Mapa com os scores calculados nos 
   * dispositivos que deseja converter
   * @return Object[]
   */
  @SuppressWarnings("unchecked")
  public static Object[] mapToArray(Map<String, Integer> map1, Map<String, Integer> map2) {
    List<JsonObject> array = new ArrayList<JsonObject>();

    for (Object object : map1.entrySet().stream().toArray()) {
      JsonObject json = new JsonObject();
      String key = ((Map.Entry<String, Integer>) object).getKey();

      json.addProperty(
        "deviceId",
        key
      );
      json.addProperty(
        "score",
        ((Map.Entry<String, Integer>) object).getValue()
      );

      if (map2.containsKey(key)) {
        json.addProperty(
          "realScore",
          map2.get(key)
        );
      }

      array.add(json);
    }

    return array.toArray();
  }
}
