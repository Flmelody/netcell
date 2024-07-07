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

package org.flmelody.netcell.core.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.AttributeKey;
import org.flmelody.netcell.MqttDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author esotericman
 */
@ChannelHandler.Sharable
public class MqttMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {
  private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
  public static final AttributeKey<Boolean> MQTT_LISTENER_FINISH =
      AttributeKey.valueOf("MQTT_LISTENER_FINISH");
  private final MqttDispatcher mqttDispatcher;

  public MqttMessageHandler(MqttDispatcher mqttDispatcher) {
    this.mqttDispatcher = mqttDispatcher;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
    MqttMessageType messageType = msg.fixedHeader().messageType();
    logger.atInfo().log("New message received, current message type: {}", messageType);
    try {
      ctx.channel().attr(MQTT_LISTENER_FINISH).setIfAbsent(Boolean.FALSE);
      mqttDispatcher.dispatch(ctx, msg);
    } finally {
      ctx.channel().attr(MQTT_LISTENER_FINISH).compareAndSet(Boolean.TRUE, Boolean.FALSE);
    }
    logger.atInfo().log("Dispatch message successful , current message type: {}", messageType);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.atError().log("Exception caught, Closing connection ", cause);
    ctx.close();
  }
}
