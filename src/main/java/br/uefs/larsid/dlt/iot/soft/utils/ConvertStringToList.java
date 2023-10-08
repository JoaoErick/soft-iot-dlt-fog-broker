package br.uefs.larsid.dlt.iot.soft.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertStringToList {

  /**
   * Converte uma String no formato de List em um objeto do tipo List.
   *
   * @param listAsString String - String que deseja converter.
   * @return List
   */
  public static List<String> convertStringToList(String listAsString) {
    return Arrays
        .stream(listAsString.split(","))
        .map(String::trim)
        .collect(Collectors.toList());
  }
} 
