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

package org.flmelody.netcell.core.provider.retained;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.flmelody.netcell.core.provider.InteractableProviderSupport;

/**
 * @author esotericman
 */
public abstract class AbstractRetainedMessageProvider
    extends InteractableProviderSupport<RetainedMessageProvider>
    implements RetainedMessageProvider {
  @Override
  public final boolean interests(MqttMessageType mqttMessageType) {
    return MqttMessageType.PUBLISH.equals(mqttMessageType);
  }

  @Override
  public void onMessage(ChannelHandlerContext context, MqttMessage mqttMessage) {
    if (mqttMessage instanceof MqttPublishMessage mqttPublishMessage) {
      if (mqttPublishMessage.fixedHeader().isRetain()) {
        String topicName = mqttPublishMessage.variableHeader().topicName();
        setRetainedMessage(topicName, mqttPublishMessage);
      }
    }
  }
}
