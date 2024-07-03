package org.flmelody;

import org.flmelody.core.netty.MqttBroker;

/**
 * @author esotericman
 */
public class MqttBrokerTest {
  public static void main(String[] args) throws Exception {
    MqttBroker mqttBroker = new MqttBroker();
    mqttBroker.start();
  }
}
