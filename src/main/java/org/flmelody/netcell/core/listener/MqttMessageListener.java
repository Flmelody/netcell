package org.flmelody.netcell.core.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.flmelody.netcell.core.handler.MqttMessageHandler;

/**
 * @author esotericman
 */
public interface MqttMessageListener {

  boolean interests(MqttMessageType mqttMessageType);

  default void onMessage(ChannelHandlerContext context, MqttMessage mqttMessage) {}

  default void markStop(ChannelHandlerContext context) {
    context
        .channel()
        .attr(MqttMessageHandler.MQTT_LISTENER_FINISH)
        .compareAndSet(Boolean.FALSE, Boolean.TRUE);
  }
}
