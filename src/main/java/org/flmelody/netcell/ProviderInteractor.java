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

import java.util.HashMap;
import java.util.Map;
import org.flmelody.netcell.core.interactor.Interactor;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.ProviderSeries;

/**
 * @author esotericman
 */
public class ProviderInteractor implements Interactor<Provider> {
  private final Map<ProviderSeries, Provider> providers = new HashMap<>();

  @Override
  public void withComponents(Provider component, boolean override) {
    if (override) {
      providers.put(component.series(), component);
    } else {
      providers.putIfAbsent(component.series(), component);
    }
  }

  public <T extends Provider> T getProvider(Class<T> type) {
    //noinspection unchecked
    return (T)
        providers.values().stream()
            .filter(provider -> type.isAssignableFrom(provider.getClass()))
            .findFirst()
            .orElse(null);
  }

  public <T extends Provider> T getProvider(ProviderSeries series, Class<T> type) {
    Provider provider = providers.get(series);
    if (provider != null && type.isAssignableFrom(provider.getClass())) {
      //noinspection unchecked
      return (T) provider;
    }
    return null;
  }
}
