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
import java.util.ServiceLoader;
import org.flmelody.netcell.core.provider.Provider;
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
  private MessageDeliveryProvider messageDeliveryProvider;
  private TemporarySessionProvider temporarySessionProvider;
  private PersistentStoreProvider persistentStoreProvider;
  private RetainedMessageProvider retainedMessageProvider;
  private SslProvider sslProvider;

  public MessageDeliveryProvider getMessageDeliveryProvider() {
    return messageDeliveryProvider;
  }

  public SslProvider getSslProvider() {
    return sslProvider;
  }

  public RetainedMessageProvider getRetainedMessageProvider() {
    return retainedMessageProvider;
  }

  public PersistentStoreProvider getPersistentStoreProvider() {
    return persistentStoreProvider;
  }

  public TemporarySessionProvider getTemporarySessionProvider() {
    return temporarySessionProvider;
  }

  // We use SPI to load providers default
  ProviderManager() {
    ServiceLoader.load(MessageDeliveryProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresentOrElse(
            provider -> messageDeliveryProvider = provider.get(),
            () -> messageDeliveryProvider = MessageDeliveryProvider.EMPTY);

    ServiceLoader.load(TemporarySessionProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresentOrElse(
            provider -> temporarySessionProvider = provider.get(),
            () -> temporarySessionProvider = TemporarySessionProvider.EMPTY);

    ServiceLoader.load(PersistentStoreProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresentOrElse(
            provider -> persistentStoreProvider = provider.get(),
            () -> persistentStoreProvider = PersistentStoreProvider.EMPTY);

    ServiceLoader.load(RetainedMessageProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresentOrElse(
            provider -> retainedMessageProvider = provider.get(),
            () -> retainedMessageProvider = RetainedMessageProvider.EMPTY);

    ServiceLoader.load(SslProvider.class).stream()
        .max(Comparator.comparing(provider -> provider.get().priority()))
        .ifPresent(provider -> sslProvider = provider.get());
  }

  /**
   * Override default SPI providers
   *
   * @param providers providers you want to use
   */
  void use(Provider... providers) {
    for (Provider provider : providers) {
      if (provider instanceof MessageDeliveryProvider) {
        messageDeliveryProvider = (MessageDeliveryProvider) provider;
      }
      if (provider instanceof TemporarySessionProvider) {
        temporarySessionProvider = (TemporarySessionProvider) provider;
      }
      if (provider instanceof PersistentStoreProvider) {
        persistentStoreProvider = (PersistentStoreProvider) provider;
      }
      if (provider instanceof RetainedMessageProvider) {
        retainedMessageProvider = (RetainedMessageProvider) provider;
      }
      if (provider instanceof SslProvider) {
        sslProvider = (SslProvider) provider;
      }
    }
  }
}
