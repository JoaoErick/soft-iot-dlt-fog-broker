package br.uefs.larsid.dlt.iot.soft.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertStringToMap {

  /**
   * Converte uma String no formato de Map em um objeto do tipo Map.
   *
   * @param mapAsString String - String que deseja converter.
   * @return Map
   */
  public static Map<String, Integer> convertStringToMap(String mapAsString) {
    return Arrays
      .stream(mapAsString.substring(1, mapAsString.length() - 1).split(", "))
      .map(entry -> entry.split("="))
      .collect(
        Collectors.toMap(entry -> entry[0], entry -> Integer.parseInt(entry[1]))
      );
  }
}
