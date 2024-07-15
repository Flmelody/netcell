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

package org.flmelody.netcell.core.provider.delivery;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.flmelody.netcell.ProviderInteractor;
import org.flmelody.netcell.core.interactor.Interactable;
import org.flmelody.netcell.core.listener.MqttMessageListener;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.ProviderSeries;

/**
 * The heart of message delivery.
 *
 * @author esotericman
 */
public interface MessageDeliveryProvider
    extends Provider,
        MqttMessageListener,
        Interactable<MessageDeliveryProvider, ProviderInteractor> {

  void publish(ChannelHandlerContext context, MqttMessage mqttMessage);

  void subscribe(ChannelHandlerContext context, MqttSubscribeMessage mqttSubscribeMessage);

  void unsubscribe(ChannelHandlerContext context, MqttUnsubscribeMessage mqttUnsubscribeMessage);

  default ProviderSeries series() {
    return ProviderSeries.DELIVERY;
  }

  MessageDeliveryProvider EMPTY = new Empty();

  /** Empty implementation */
  class Empty implements MessageDeliveryProvider {
    @Override
    public boolean interests(MqttMessageType mqttMessageType) {
      return false;
    }

    @Override
    public void publish(ChannelHandlerContext context, MqttMessage mqttMessage) {}

    @Override
    public void subscribe(
        ChannelHandlerContext context, MqttSubscribeMessage mqttSubscribeMessage) {}

    @Override
    public void unsubscribe(
        ChannelHandlerContext context, MqttUnsubscribeMessage mqttUnsubscribeMessage) {}

    @Override
    public MessageDeliveryProvider withActor(ProviderInteractor interactor) {
      return this;
    }
  }
}
