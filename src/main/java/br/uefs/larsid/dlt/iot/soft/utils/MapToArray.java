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
}
