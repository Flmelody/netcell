package org.flmelody;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.UUID;

/**
 * @author esotericman
 */
public class MqttV3ClientTest {
  public static void main(String[] args) throws MqttException {
    IMqttClient publisher =
        new MqttClient("tcp://localhost:1883", UUID.randomUUID().toString());
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);
      options.setConnectionTimeout(10);
      publisher.connect(options);
  }
}
