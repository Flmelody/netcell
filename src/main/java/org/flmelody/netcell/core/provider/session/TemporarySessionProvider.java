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
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.flmelody.netcell.ProviderInteractor;
import org.flmelody.netcell.core.interactor.Interactable;
import org.flmelody.netcell.core.listener.MqttMessageListener;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.ProviderSeries;

/**
 * Storage of client sessions.
 *
 * @author esotericman
 */
public interface TemporarySessionProvider
    extends Provider,
        MqttMessageListener,
        Interactable<TemporarySessionProvider, ProviderInteractor> {

  void connect(MqttConnectMessage mqttConnectMessage, ChannelHandlerContext context);

  void disconnect(MqttMessage mqttMessage, ChannelHandlerContext context);

  ChannelHandlerContext client(String clientId);

  default ProviderSeries series() {
    return ProviderSeries.SESSION;
  }

  TemporarySessionProvider EMPTY = new Empty();

  /** Empty implementation */
  class Empty implements TemporarySessionProvider {
    @Override
    public boolean interests(MqttMessageType mqttMessageType) {
      return false;
    }

    @Override
    public void connect(MqttConnectMessage mqttConnectMessage, ChannelHandlerContext context) {}

    @Override
    public void disconnect(MqttMessage mqttMessage, ChannelHandlerContext context) {}

    @Override
    public ChannelHandlerContext client(String clientId) {
      return null;
    }

    @Override
    public TemporarySessionProvider withActor(ProviderInteractor interactor) {
      return this;
    }
  }
}
