package br.uefs.larsid.dlt.iot.soft.utils;

import java.util.List;

import br.uefs.larsid.dlt.iot.soft.entity.Sensor;

public class ConvertValueToScore {

    public static synchronized int calculateDeviceScore(List<Sensor> sensors) {
        int sumValues = 0;

        for (Sensor sensor : sensors) {
            sumValues += calculateSensorScore(sensor.getType(), sensor.getValue());
        }

        return sumValues;
    }

    public static synchronized int calculateSensorScore(String sensorType, int value) {
        int score;

        switch (sensorType) {
            case "RespirationRateSensor":
                if (value >= 12 && value <= 20) {
                    score = 0;
                } else if (value >= 21 && value <= 24) {
                    score = 1;
                } else if (value >= 25 && value <= 30) {
                    score = 2;
                } else {
                    score = 3;
                }

                break;

            case "PulseOxymeterSensor":
                if (value >= 96) {
                    score = 0;
                } else if (value == 94 || value == 95) {
                    score = 1;
                } else if (value == 92 || value == 93) {
                    score = 2;
                } else {
                    score = 3;
                }

                break;

            case "BodyThermometerSensor":
                if (value >= 36 && value <= 38) {
                    score = 0;
                } else if (value == 39) {
                    score = 1;
                } else if (value == 40 || value == 35) {
                    score = 2;
                } else {
                    score = 3;
                }

                break;

            case "HeartRateSensor":
                if (value >= 51 && value <= 90) {
                    score = 0;
                } else if ((value >= 91 && value <= 110) || (value >= 41 && value <= 50)) {
                    score = 1;
                } else if ((value >= 111 && value <= 130) || (value >= 31 && value <= 40)) {
                    score = 2;
                } else {
                    score = 3;
                }

                break;
        
            default:
                String message = String.format(
                    "Oops! Unrecognized sensor type '%s'... Assigning the default score (0).", 
                    sensorType
                );

                System.out.println(message);
                score = 0;

                break;
        }

        return score;
    }
}
