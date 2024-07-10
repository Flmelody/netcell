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

package org.flmelody.netcell.core.provider.persistence;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.flmelody.netcell.ProviderInteractor;
import org.flmelody.netcell.core.interactor.Interactable;
import org.flmelody.netcell.core.listener.MqttMessageListener;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.ProviderSeries;

/**
 * The persistence of messages should be implemented in a way that ensures as high a quality as
 * possible.
 *
 * @see MqttQoS
 * @author esotericman
 */
public interface PersistentStoreProvider
    extends Provider,
        MqttMessageListener,
        Interactable<PersistentStoreProvider, ProviderInteractor> {

  default ProviderSeries series() {
    return ProviderSeries.PERSISTENCE;
  }

  PersistentStoreProvider EMPTY = new Empty();

  /** Empty implementation */
  class Empty implements PersistentStoreProvider {
    @Override
    public boolean interests(MqttMessageType mqttMessageType) {
      return false;
    }

    @Override
    public PersistentStoreProvider withActor(ProviderInteractor interactor) {
      return this;
    }
  }
}
