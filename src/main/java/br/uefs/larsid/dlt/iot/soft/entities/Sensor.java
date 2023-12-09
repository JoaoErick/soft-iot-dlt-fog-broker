package br.uefs.larsid.dlt.iot.soft.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.uefs.larsid.dlt.iot.soft.models.ClientIotService;

import org.json.JSONObject;

public class Sensor {

  private String id;
  private String type;
  private int value;
  private String deviceAPIAddress;

  @JsonProperty("collection_time")
  private int collectionTime;

  @JsonProperty("publishing_time")
  private int publishingTime;

  public Sensor() {
  }

  /**
   * Atualiza o valor do sensor.
   *
   * @param idDevice String - Id do dispositivo.
   */
  public void getValue(String idDevice) {
    String url = String.format("%s/%s/%s", deviceAPIAddress, idDevice, this.id);
    String response = ClientIotService.getApiIot(url);

    if (response != null) {
      JSONObject json = new JSONObject(response);
      this.value = Integer.valueOf(json.getString("value"));
    } else {
      this.value = 0;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getCollectionTime() {
    return collectionTime;
  }

  public void setCollectionTime(int collectionTime) {
    this.collectionTime = collectionTime;
  }

  public int getPublishingTime() {
    return publishingTime;
  }

  public void setPublishingTime(int publishingTime) {
    this.publishingTime = publishingTime;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public String getdeviceAPIAddress() {
    return deviceAPIAddress;
  }

  public void setdeviceAPIAddress(String deviceAPIAddress) {
    this.deviceAPIAddress = deviceAPIAddress;
  }
}
