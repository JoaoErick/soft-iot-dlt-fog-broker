package br.uefs.larsid.dlt.iot.soft.config;

import java.util.Properties;

public class Config {
  // TODO Corrigir path do arquivo de configuração
  /*-------------------------Constantes---------------------------------------*/
  private static final String FILE = "src/main/resources/br.uefs.larsid.dlt.soft_iot.fog_broker.cfg";
  /*--------------------------------------------------------------------------*/

  Properties configFile;

  public Config() {
    this.configFile = new java.util.Properties();
    
    try {
      this.configFile.load(
        this.getClass().getClassLoader().getResourceAsStream(FILE)
      );
    } catch (Exception eta) {
      eta.printStackTrace();
    }
  }

  public String getProperty(String key) {
    return this.configFile.getProperty(key);
  }
}
