package br.uefs.larsid.dlt.iot.soft.model;

import java.util.concurrent.atomic.AtomicBoolean;

import br.uefs.larsid.dlt.iot.soft.mqtt.MQTTClient;
import br.uefs.larsid.dlt.iot.soft.services.Controller;

public class ThreadPing implements Runnable {
    /*-------------------------Constantes---------------------------------------*/
    private static final int PING_TIME = 10000; // TODO: Alterar delay.
    private static final int QOS = 1;
    /*--------------------------------------------------------------------------*/

    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private boolean debugModeValue;
    private Controller controllerImpl;
    private String mqttUser;
    private String mqttPassword;
    private String topic;

    public ThreadPing(
        Controller controllerImpl,
        String mqttUser,
        String mqttPassword,
        String topic,
        boolean debugModeValue
    ) {
        this.debugModeValue = debugModeValue;
        this.controllerImpl = controllerImpl;
        this.mqttUser = mqttUser;
        this.mqttPassword = mqttPassword;
        this.topic = topic;
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                Thread.sleep(PING_TIME);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(
                        "Thread was interrupted, Failed to complete operation");
            }
            if (this.controllerImpl.getNodeUriList().size() > 0) {
                for (String nodeUri : this.controllerImpl.getNodeUriList()) {
                    printlnDebug(nodeUri);
                    String uri[] = nodeUri.split(":");

                    MQTTClient MQTTClientDown = new MQTTClient(
                            debugModeValue,
                            uri[0],
                            uri[1],
                            this.mqttUser,
                            this.mqttPassword);

                    MQTTClientDown.connect();
                    MQTTClientDown.publish(this.topic, "".getBytes(), QOS);
                    MQTTClientDown.disconnect();
                }
            }
        }
    }

    private void printlnDebug(String str) {
        if (debugModeValue) {
            System.out.println(str);
        }
    }
}
