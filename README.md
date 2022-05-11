# soft-iot-dlt-fog-broker

O `soft-iot-dlt-fog-broker` é o *bundle* genérico que pode atuar tanto na camada *Edge* quanto na *Fog*. Ele responsável por realizar o cálculo de [Top-K](https://www.sciencedirect.com/science/article/abs/pii/S002002551830714X#:~:text=A%20Top-k%20retrieval%20algorithm%20returns%20the%20k%20best%20answers,take%20into%20consideration%20execution%20time.) dos dispositivos a partir de uma requisição feita por um *Client* superior. <br/>
Para a comunicação, é utilizado o protocolo MQTT. 

### Modelo da arquitetura

<p align="center">
  <img src="./assets/architecture-diagram-fog-broker.png" width="580px" />
</p>

## Configurações

Propriedade | Descrição | Valor Padrão
------------|-----------|-------------
ip_up | Endereço IP do *Client* situado na camada acima | localhost
ip | Endereço IP de onde o *Bundle* está sendo executado | localhost 
ip_down¹ | Endereço IP do *Client* situado na camada abaixo | localhost
port | Porta para conexão com o *Broker* | 1883
user | Usuário para conexão com o *Broker* | karaf
pass | Senha para conexão com o *Broker* | karaf
urlAPI | URL da API onde estão os dispositivos | http://localhost:8181/cxf/iot-service/devices
debugModeValue | Modo depuração | true
hasNodes | Se o bundle irá possuir filhos | true

###### Obs¹: Caso o *Bundle* esteja sendo executado na camada mais baixa (`nodes=0`), a configuração pode ser mantida como: `ip_down=localhost`;
