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

package org.flmelody.netcell.core.provider;

import java.util.Objects;
import org.flmelody.netcell.ProviderInteractor;
import org.flmelody.netcell.core.interactor.Interactable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the ability for providers to interact with each other.
 *
 * @author esotericman
 */
public abstract class InteractableProviderSupport<T extends Provider>
    implements Provider, Interactable<T, ProviderInteractor> {
  protected Logger logger = LoggerFactory.getLogger(getClass());
  // Interactor for providers communicating with each other
  protected final InteractorFacade providerInteractor = new InteractorFacade();

  @Override
  public final T withActor(ProviderInteractor interactor) {
    if (Objects.isNull(this.providerInteractor.interactor)) {
      this.providerInteractor.interactor = interactor;
      this.providerInteractor.interactor.withComponents(this, true);
    }
    //noinspection unchecked
    return (T) this;
  }

  /** Interactor facade, Only basic interactions are provided. */
  public static class InteractorFacade {
    private ProviderInteractor interactor;

    private InteractorFacade() {}

    public <T extends Provider> T getProvider(Class<T> type) {
      return this.interactor.getProvider(type);
    }

    public <T extends Provider> T getProvider(ProviderSeries series, Class<T> type) {
      return this.interactor.getProvider(series, type);
    }
  }
}
