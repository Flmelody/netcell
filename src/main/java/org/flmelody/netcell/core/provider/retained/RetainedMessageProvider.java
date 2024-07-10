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

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.flmelody.netcell.ProviderInteractor;
import org.flmelody.netcell.core.interactor.Interactable;
import org.flmelody.netcell.core.listener.MqttMessageListener;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.ProviderSeries;

/**
 * Reserve a messages for future subscribers.
 *
 * @author esotericman
 */
public interface RetainedMessageProvider
    extends Provider,
        MqttMessageListener,
        Interactable<RetainedMessageProvider, ProviderInteractor> {

  /**
   * Try to get retained message, maybe null if there aren't retained message published yet.
   *
   * @param topic topic
   * @return retained message
   */
  MqttMessage getRetainedMessage(String topic);

  /**
   * Set retained message of specific topic.
   *
   * @param topic topic
   * @param mqttMessage message
   */
  void setRetainedMessage(String topic, MqttMessage mqttMessage);

  default ProviderSeries series() {
    return ProviderSeries.RETAINED;
  }

  RetainedMessageProvider EMPTY = new Empty();

  /** Empty implementation */
  class Empty implements RetainedMessageProvider {
    @Override
    public MqttMessage getRetainedMessage(String topic) {
      return null;
    }

    @Override
    public void setRetainedMessage(String topic, MqttMessage mqttMessage) {}

    @Override
    public boolean interests(MqttMessageType mqttMessageType) {
      return false;
    }

    @Override
    public RetainedMessageProvider withActor(ProviderInteractor interactor) {
      return this;
    }
  }
}
