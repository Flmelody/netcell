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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import org.flmelody.netcell.core.interactor.Interactable;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.ProviderSeries;
import org.flmelody.netcell.core.provider.delivery.MessageDeliveryProvider;
import org.flmelody.netcell.core.provider.persistence.PersistentStoreProvider;
import org.flmelody.netcell.core.provider.retained.RetainedMessageProvider;
import org.flmelody.netcell.core.provider.security.SslProvider;
import org.flmelody.netcell.core.provider.session.TemporarySessionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author esotericman
 */
public final class ProviderManager implements Provider {
  private static final Logger logger = LoggerFactory.getLogger(ProviderManager.class);
  private static final Map<ProviderSeries, Provider> providers = new HashMap<>();
  private final ProviderInteractor interactor = new ProviderInteractor();

  // We use SPI to load providers default
  ProviderManager() {
    ServiceLoader.load(MessageDeliveryProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresent(
            provider ->
                providers.put(provider.get().withActor(this.interactor).series(), provider.get()));

    ServiceLoader.load(TemporarySessionProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresent(
            provider ->
                providers.put(provider.get().withActor(this.interactor).series(), provider.get()));

    ServiceLoader.load(PersistentStoreProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresent(
            provider ->
                providers.put(provider.get().withActor(this.interactor).series(), provider.get()));

    ServiceLoader.load(RetainedMessageProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresent(
            provider ->
                providers.put(provider.get().withActor(this.interactor).series(), provider.get()));

    ServiceLoader.load(SslProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresent(provider -> providers.put(provider.get().series(), provider.get()));
  }

  /**
   * Override default SPI providers
   *
   * @param providers providers you want to use
   */
  void use(Provider... providers) {
    for (Provider provider : providers) {
      //noinspection rawtypes
      if (provider instanceof Interactable interactable) {
        //noinspection unchecked
        interactable.withActor(this.interactor);
      }
      ProviderManager.providers.put(provider.series(), provider);
    }
  }

  static Provider provider(ProviderSeries series) {
    return providers.get(series);
  }

  static <T extends Provider> T provider(
      ProviderSeries series, Class<T> clazz, Supplier<T> supplier) {
    Provider provider = providers.get(series);
    if (Objects.nonNull(provider)) {
      if (clazz.isAssignableFrom(provider.getClass())) {
        //noinspection unchecked
        return (T) provider;
      }
    }
    return supplier.get();
  }

  static <T extends Provider> T provider(ProviderSeries series, Class<T> clazz, T defaultProvider) {
    Provider provider = providers.get(series);
    if (Objects.nonNull(provider)) {
      if (clazz.isAssignableFrom(provider.getClass())) {
        //noinspection unchecked
        return (T) provider;
      }
    }
    return defaultProvider;
  }

  @Override
  public ProviderSeries series() {
    return null;
  }
}
