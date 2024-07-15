package org.flmelody.netcell.core.provider.delivery;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.flmelody.netcell.core.constants.NettyAttributeKeys;
import org.flmelody.netcell.core.provider.ProviderSeries;
import org.flmelody.netcell.core.provider.retained.RetainedMessageProvider;
import org.flmelody.netcell.core.provider.session.TemporarySessionProvider;

/**
 * @author esotericman
 */
public class LocalMessageDeliveryProvider extends AbstractMessageDeliveryProvider {
  private static final Map<String, Set<String>> subscribers = new ConcurrentHashMap<>();

  @Override
  public void publish(ChannelHandlerContext context, MqttMessage mqttMessage) {
    if (mqttMessage instanceof MqttPublishMessage publishMessage) {
      String topicName = publishMessage.variableHeader().topicName();
      int packetId = publishMessage.variableHeader().packetId();
      MqttQoS mqttQoS = publishMessage.fixedHeader().qosLevel();
      Set<String> clients = subscribers.get(topicName);
      TemporarySessionProvider provider =
          providerInteractor.getProvider(ProviderSeries.SESSION, TemporarySessionProvider.class);
      if (Objects.isNull(provider)) {
        logger.warn("No provider found for session, ignored message delivery");
        return;
      }
      if (Objects.nonNull(clients)) {
        for (String client : clients) {
          provider.client(client).writeAndFlush(publishMessage);
        }
      }
      if (MqttQoS.AT_LEAST_ONCE.equals(mqttQoS)) {
        MqttFixedHeader fixedHeader =
            new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(packetId);
        context.writeAndFlush(new MqttPubAckMessage(fixedHeader, idVariableHeader));
      }
    }
  }

  @Override
  public synchronized void subscribe(
      ChannelHandlerContext context, MqttSubscribeMessage mqttSubscribeMessage) {
    String clientId = context.channel().attr(NettyAttributeKeys.MQTT_CLIENT_ID).get();
    mqttSubscribeMessage
        .payload()
        .topicSubscriptions()
        .forEach(
            topicSubscription ->
                subscribers
                    .computeIfAbsent(topicSubscription.topicFilter(), k -> new HashSet<>())
                    .add(clientId));
    RetainedMessageProvider provider =
        providerInteractor.getProvider(ProviderSeries.RETAINED, RetainedMessageProvider.class);
    MqttFixedHeader fixedHeader =
        new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
    MqttMessageIdVariableHeader idVariableHeader =
        MqttMessageIdVariableHeader.from(mqttSubscribeMessage.variableHeader().messageId());
    MqttSubAckPayload subAckPayload = new MqttSubAckPayload(MqttQoS.AT_MOST_ONCE.value());
    context.writeAndFlush(new MqttSubAckMessage(fixedHeader, idVariableHeader, subAckPayload));
    if (Objects.nonNull(provider)) {
      MqttMessage retainedMessage = provider.getRetainedMessage("topic");
      if (Objects.nonNull(retainedMessage)) {
        TemporarySessionProvider sessionProvider =
            providerInteractor.getProvider(ProviderSeries.SESSION, TemporarySessionProvider.class);
        if (Objects.nonNull(sessionProvider)) {
          sessionProvider.client(clientId).writeAndFlush(retainedMessage);
        }
      }
    }
  }

  @Override
  public void unsubscribe(
      ChannelHandlerContext context, MqttUnsubscribeMessage mqttUnsubscribeMessage) {
    String clientId = context.channel().attr(NettyAttributeKeys.MQTT_CLIENT_ID).get();
    mqttUnsubscribeMessage
        .payload()
        .topics()
        .forEach(
            topic -> {
              if (subscribers.containsKey(topic)) {
                subscribers.get(topic).remove(clientId);
              }
            });
  }
}
