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

    public List<Device> getDevices();

    public void setDevices(List<Device> devices);

    public List<String> getAuthenticatedDevicesIds();

    public void setAuthenticatedDevicesIds(List<String> authenticatedDevicesIds);

    public int getCheckDeviceTaskTime();

    public void setCheckDeviceTaskTime(int checkDeviceTaskTime);

    public String getDeviceAPIAddress();

    public void setDeviceAPIAddress(String deviceAPIAddress);

    /**
     * Verifica se o gateway possui o serviço de identidade habilitado.
     *
     * @return boolean
     */
    public boolean hasIdentityService();

    public void setHasIdentityService(boolean hasIdentityService);
}
