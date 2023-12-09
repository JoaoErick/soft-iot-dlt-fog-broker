package br.uefs.larsid.dlt.iot.soft.tasks;

import java.util.TimerTask;
import java.util.logging.Logger;

import br.uefs.larsid.dlt.iot.soft.models.Node;

public class CheckDevicesTask extends TimerTask {
    private final Node node;
    private static final Logger logger = Logger.getLogger(
        CheckDevicesTask.class.getName()
    );

    /**
     * Método construtor.
     *
     * @param node NodeType - Nó que verificará os dispositivos que estão
     * conectados.
     */
    public CheckDevicesTask(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        logger.info("...Checking connected devices...");

        try {
            this.node.loadConnectedDevices();
        } catch (Exception e) {
            logger.severe("Unable to update device list.");
            logger.severe(e.getStackTrace().toString());
            this.cancel();
        }
    }
}
