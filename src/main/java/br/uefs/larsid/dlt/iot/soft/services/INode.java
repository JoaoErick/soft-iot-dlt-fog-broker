package br.uefs.larsid.dlt.iot.soft.services;

import java.util.List;

import br.uefs.larsid.dlt.iot.soft.entities.Device;

public interface INode {
    /**
     * Adiciona os dispositivos que foram requisitados na lista de dispositivos.
     *
     * @param strDevices String - Dispositivos requisitados.
     */
    public void loadConnectedDevices();

    /**
     * Obtém a lista de dispositivos virtuais.
     *
     * @return List<String>
     */
    public List<Device> getDevices();

    public void setDevices(List<Device> devices);

    /**
     * Obtém a lista de dispositivos virtuais autenticados.
     *
     * @return List<String>
     */
    public List<String> getAuthenticatedDevicesIds();

    public void setAuthenticatedDevicesIds(List<String> authenticatedDevicesIds);

    /**
     * Obtém o valor do intervalo definido para verificar a quantidade de 
     * dispositivos virtuais conectados.
     *
     * @return int
     */
    public int getCheckDeviceTaskTime();

    public void setCheckDeviceTaskTime(int checkDeviceTaskTime);

    /**
     * Obtém o endereço da API dos dispositivos virtuais.
     *
     * @return String
     */
    public String getDeviceAPIAddress();

    public void setDeviceAPIAddress(String deviceAPIAddress);
    
    /**
     * Verifica se o gateway está em modo de encaminhamento ou processamento 
     * de dados.
     *
     * @return boolean
     */
    public boolean isForwardingGateway();

    public void setIsForwardingGateway(boolean isForwardingGateway);

    /**
     * Verifica se o gateway possui o serviço de identidade habilitado.
     *
     * @return boolean
     */
    public boolean hasIdentityService();

    public void setHasIdentityService(boolean hasIdentityService);

    /**
     * Verifica se o gateway deve coletar os scores reais dos dispositivos.
     * 
     * @return boolean
     */
    public boolean hasCollectRealScoreService();

    public void setHasCollectRealScoreService(boolean hasCollectRealScoreService);
}
