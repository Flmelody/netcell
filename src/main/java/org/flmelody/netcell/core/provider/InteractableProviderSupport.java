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
import org.flmelody.netcell.core.interactor.Interactor;

/**
 * @author esotericman
 */
public abstract class InteractableProviderSupport<T extends Provider>
    implements Provider, Interactable<T, Provider> {
  protected final DelegateInteractor providerInteractor = new DelegateInteractor();

  @Override
  public final T withActor(Interactor<Provider> interactor) {
    if (Objects.isNull(this.providerInteractor.interactor)) {
      this.providerInteractor.interactor = interactor;
    }
    this.providerInteractor.withComponents(this);
    //noinspection unchecked
    return (T) this;
  }

  /** Delegate inter actor */
  public static class DelegateInteractor extends ProviderInteractor {
    private Interactor<Provider> interactor;

    @Override
    public void withComponents(Provider component) {
      interactor.withComponents(component);
    }

    @Override
    public <T extends Provider> T getProvider(Class<T> type) {
      if (this.interactor instanceof ProviderInteractor) {
        return ((ProviderInteractor) this.interactor).getProvider(type);
      }
      return super.getProvider(type);
    }

    @Override
    public <T extends Provider> T getProvider(ProviderSeries series, Class<T> type) {
      if (this.interactor instanceof ProviderInteractor) {
        return ((ProviderInteractor) this.interactor).getProvider(series, type);
      }
      return super.getProvider(series, type);
    }
  }
}
