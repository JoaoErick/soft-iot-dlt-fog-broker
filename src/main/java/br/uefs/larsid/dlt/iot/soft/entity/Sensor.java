package br.uefs.larsid.dlt.iot.soft.entity;

import br.uefs.larsid.dlt.iot.soft.model.ClientIotService;
import org.json.JSONObject;

public class Sensor {

  private String id;
  private String type;
  private int collectionTime;
  private int publishingTime;
  private int value;
  private String urlAPI;

  public Sensor(String urlAPI) {
    this.urlAPI = urlAPI;
  }

  public void getValue(String idDevice) {
    String url = String.format("%s/%s/%s", urlAPI, idDevice, this.id);
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
}
