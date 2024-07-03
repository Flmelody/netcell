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

package org.flmelody.core.netty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author esotericman
 */
@ChannelHandler.Sharable
public class MqttMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
    MqttMessageType messageType = msg.fixedHeader().messageType();
    System.out.println("New message received");
    System.out.println(messageType);
    if (messageType == MqttMessageType.CONNECT) {
      MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) msg;
      MqttConnectVariableHeader mqttConnectVariableHeader = mqttConnectMessage.variableHeader();
      MqttFixedHeader fixedHeader =
          new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
      MqttConnAckVariableHeader variableHeader =
          new MqttConnAckVariableHeader(
              MqttConnectReturnCode.CONNECTION_ACCEPTED,
              !mqttConnectVariableHeader.isCleanSession());
      MqttConnAckMessage mqttConnAckMessage = new MqttConnAckMessage(fixedHeader, variableHeader);
      ctx.writeAndFlush(mqttConnAckMessage);
    }
  }
}
