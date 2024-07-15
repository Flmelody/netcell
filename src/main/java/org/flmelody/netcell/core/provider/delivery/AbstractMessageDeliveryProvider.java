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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.flmelody.netcell.core.provider.InteractableProviderSupport;

/**
 * @author esotericman
 */
public abstract class AbstractMessageDeliveryProvider
    extends InteractableProviderSupport<MessageDeliveryProvider>
    implements MessageDeliveryProvider {
  private static final Set<MqttMessageType> supportedMessageTypes =
      new HashSet<>(
          Arrays.asList(
              MqttMessageType.PUBLISH,
              MqttMessageType.PUBREL,
              MqttMessageType.SUBSCRIBE,
              MqttMessageType.UNSUBSCRIBE));

  @Override
  public final boolean interests(MqttMessageType mqttMessageType) {
    return supportedMessageTypes.contains(mqttMessageType);
  }

  @Override
  public void onMessage(ChannelHandlerContext context, MqttMessage mqttMessage) {
    MqttMessageType mqttMessageType = mqttMessage.fixedHeader().messageType();

    switch (mqttMessageType) {
      case PUBLISH:
        publish(context, mqttMessage);
        break;
      case PUBREL:
        break;
      case SUBSCRIBE:
        subscribe(context, (MqttSubscribeMessage) mqttMessage);
        break;
      case UNSUBSCRIBE:
        unsubscribe(context, (MqttUnsubscribeMessage) mqttMessage);
        break;
      default:
    }
  }
}
