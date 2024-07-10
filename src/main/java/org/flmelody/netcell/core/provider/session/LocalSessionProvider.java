/*
 * Copyright (C) 2023 Flmelody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flmelody.netcell.core.provider.session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.flmelody.netcell.core.constants.NettyAttributeKeys;

/**
 * @author esotericman
 */
public class LocalSessionProvider extends AbstractTemporarySessionProvider {
  protected static final Map<String, ChannelHandlerContext> clients =
      new ConcurrentHashMap<>(2 << 10);

  @Override
  public void connect(MqttConnectMessage mqttConnectMessage, ChannelHandlerContext context) {
    clients.put(mqttConnectMessage.payload().clientIdentifier(), context);
    context
        .channel()
        .attr(NettyAttributeKeys.MQTT_CLIENT_ID)
        .set(mqttConnectMessage.payload().clientIdentifier());
    MqttConnectVariableHeader mqttConnectVariableHeader = mqttConnectMessage.variableHeader();
    MqttFixedHeader fixedHeader =
        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
    MqttConnAckVariableHeader variableHeader =
        new MqttConnAckVariableHeader(
            MqttConnectReturnCode.CONNECTION_ACCEPTED, !mqttConnectVariableHeader.isCleanSession());
    MqttConnAckMessage mqttConnAckMessage = new MqttConnAckMessage(fixedHeader, variableHeader);
    context.writeAndFlush(mqttConnAckMessage);
  }

  @Override
  public void disconnect(MqttMessage mqttMessage, ChannelHandlerContext context) {
    clients.remove(context.channel().attr(NettyAttributeKeys.MQTT_CLIENT_ID).get());
  }

  @Override
  public ChannelHandlerContext client(String clientId) {
    return clients.get(clientId);
  }
}
