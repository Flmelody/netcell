package org.flmelody.netcell.core.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * @author esotericman
 */
public class MqttPingMessageListener implements MqttMessageListener {
  @Override
  public boolean interests(MqttMessageType mqttMessageType) {
    return MqttMessageType.PINGREQ.equals(mqttMessageType);
  }

  @Override
  public void onMessage(ChannelHandlerContext context, MqttMessage mqttMessage) {
    context.writeAndFlush(MqttMessage.PINGRESP);
  }
}
