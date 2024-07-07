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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author esotericman
 */
public abstract class AbstractPersistentStoreProvider implements PersistentStoreProvider {
  private static final Set<MqttMessageType> supportedMessageTypes =
      new HashSet<>(Arrays.asList(MqttMessageType.CONNECT, MqttMessageType.PUBLISH));

  @Override
  public final boolean interests(MqttMessageType mqttMessageType) {
    return supportedMessageTypes.contains(mqttMessageType);
  }
}
