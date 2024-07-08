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

package org.flmelody.netcell;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import java.util.ArrayList;
import java.util.List;
import org.flmelody.netcell.core.constants.NettyAttributeKeys;
import org.flmelody.netcell.core.listener.MqttMessageListener;
import org.flmelody.netcell.core.listener.MqttPingMessageListener;

/**
 * @author esotericman
 */
public final class MqttDispatcher {
  private final List<MqttMessageListener> mqttMessageListeners = new ArrayList<>();

  MqttDispatcher assembleListeners(ProviderManager providerManager) {
    mqttMessageListeners.add(new MqttPingMessageListener());
    mqttMessageListeners.add(providerManager.getTemporarySessionProvider());
    mqttMessageListeners.add(providerManager.getMessageDeliveryProvider());
    mqttMessageListeners.add(providerManager.getRetainedMessageProvider());
    mqttMessageListeners.add(providerManager.getPersistentStoreProvider());
    return this;
  }

  public void dispatch(ChannelHandlerContext context, MqttMessage mqttMessage) {
    MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
    for (MqttMessageListener mqttMessageListener : mqttMessageListeners) {
      if (context.channel().attr(NettyAttributeKeys.MQTT_LISTENER_FINISH).get()) {
        if (mqttMessageListener.interests(messageType)) {
          mqttMessageListener.onMessage(context, mqttMessage);
        }
      }
    }
  }
}
