package br.uefs.larsid.dlt.iot.soft.listener;

import br.uefs.larsid.dlt.iot.soft.services.Controller;
import org.osgi.framework.ServiceReference;

public class MappingDevicesListener {

  private boolean debugModeValue;
  private Controller controllerImpl;

  public void onBind(ServiceReference ref) {
    printlnDebug("Bound service: " + ref);
    this.controllerImpl.loadConnectedDevices();
  }

  public void onUnbind(ServiceReference ref) {
    printlnDebug("Unbound service: " + ref);
  }

  private void printlnDebug(String str) {
    if (this.debugModeValue) {
      System.out.println(str);
    }
  }

  public boolean isDebugModeValue() {
    return debugModeValue;
  }

  public void setDebugModeValue(boolean debugModeValue) {
    this.debugModeValue = debugModeValue;
  }

  public Controller getControllerImpl() {
    return controllerImpl;
  }

  public void setControllerImpl(Controller controllerImpl) {
    this.controllerImpl = controllerImpl;
  }
}
