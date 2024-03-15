# soft-iot-dlt-fog-broker

O `soft-iot-dlt-fog-broker` é o _bundle_ genérico que pode atuar tanto na camada _Edge_ quanto na _Fog_. Ele responsável por realizar o cálculo de [Top-K](https://www.sciencedirect.com/science/article/abs/pii/S002002551830714X#:~:text=A%20Top-k%20retrieval%20algorithm%20returns%20the%20k%20best%20answers,take%20into%20consideration%20execution%20time.) dos dispositivos a partir de uma requisição feita por um _Client_ superior. <br/>
Para a comunicação, é utilizado o protocolo MQTT.

### Modelo da arquitetura

<p align="center">
  <img src="./assets/architecture-diagram-fog-broker.png" width="580px" />
</p>

## Configurações

| Propriedade      | Descrição                                                          | Valor Padrão                                  |
| ---------------- | ------------------------------------------------------------------ | --------------------------------------------- |
| ipUp             | Endereço IP do _Client_ situado na camada acima                    | localhost                                     |
| ip               | Endereço IP de onde o _Bundle_ está sendo executado                | localhost                                     |
| port             | Porta para conexão com o _Broker_                                  | 1883                                          |
| user             | Usuário para conexão com o _Broker_                                | karaf                                         |
| pass             | Senha para conexão com o _Broker_                                  | karaf                                         |
| deviceAPIAddress | URL da API onde estão os dispositivos                              | http://localhost:8181/cxf/iot-service/devices |
| isForwardingGateway | Se o gateway está em modo de encaminhamento                     | false                                         |
| hasNodes         | Se o gateway onde o bundle está sendo executado possuirá filhos    | true                                          |
| timeoutInSeconds | Tempo máximo de espera da resposta dos filhos                      | 30                                            |
| checkDeviceTaskTime | Intervalo de verificação da quantidade de dispositivos conectados | 20                                          |
| hasIdentityService | Se o gateway onde o bundle está sendo executado possuirá o serviço de identidade | false                         |
| debugModeValue   | Modo depuração                                                     | true                                          |

## Protocolo TopKP

<details>
<summary><h3>Requisita quais são os sensores disponíveis:</h3></summary>

**Requisição:**

```
GET sensors
```

**Resposta**

```powershell
{
    "sensors": [
        "sensorType1",
        "sensorType2",
        "sensorType3"
    ]
}
```

</details>

<details>
<summary><h3>Requisita os Top-Ks dispositivos:</h3></summary>

**Requisição:**

```powershell
GET topk {
    "id": "requestId",
    "k": value,
    "functionHealth": [
        {
            "sensor": "sensorType1",
            "weight": value
        },
        {
            "sensor": "sensorType2",
            "weight": value
        },
        {
            "sensor": "sensorType3",
            "weight": "value
        },
    ]
}
```

**Resposta**

```powershell
{
    "id": "responseId",
    "timestamp": "currentTime",
    "devices": [
        {
            "deviceId": "deviceId1",
            "score": value
        },
        {
            "deviceId": "deviceId2",
            "score": value
        },
    ]
}
```

</details>

## Requisições através do [Mosquitto](https://mosquitto.org/) _MQTT Broker_

### Requisita quais são os sensores disponíveis

```powershell
mosquitto_sub -t "SENSORS_FOG_RES/" -u <USER> -P <PASSWORD -h <HOST_NAME> -p <PORT>
```

```powershell
mosquitto_pub -t "GET sensors" -m "" -u <USER> -P <PASSWORD -h <HOST_NAME> -p <PORT>
```

### Requisita os Top-Ks dispositivos

```powershell
mosquitto_sub -t "TOP_K_HEALTH_FOG_RES/#" -u <USER> -P <PASSWORD> -h <HOST_NAME> -p <PORT>
```

```powershell
mosquitto_pub -t "GET topk" -m '{"id": value,"k": value,"functionHealth": [{"sensor": "sensorType1","weight": value},{"sensor": "sensorType2","weight": value}]}' -u <USER> -P <PASSWORD> -h <HOST_NAME> -p <PORT>
```
