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

package org.flmelody.netcell.core.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.flmelody.netcell.core.constants.NettyAttributeKeys;

/**
 * @author esotericman
 */
public interface MqttMessageListener {

  boolean interests(MqttMessageType mqttMessageType);

  default void onMessage(ChannelHandlerContext context, MqttMessage mqttMessage) {}

  default void markStop(ChannelHandlerContext context) {
    context
        .channel()
        .attr(NettyAttributeKeys.MQTT_LISTENER_FINISH)
        .compareAndSet(Boolean.FALSE, Boolean.TRUE);
  }
}
