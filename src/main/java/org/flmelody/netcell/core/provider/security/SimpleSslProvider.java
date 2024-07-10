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

package org.flmelody.netcell.core.provider.security;

import java.io.InputStream;
import java.util.Objects;
import org.flmelody.netcell.core.provider.ProviderSeries;

/**
 * @author esotericman
 */
public final class SimpleSslProvider implements SslProvider {
  private InputStream certFile;
  private InputStream keyFile;

  private SimpleSslProvider() {}

  public static SslProviderBuilder builder() {
    return new SslProviderBuilder(new SimpleSslProvider());
  }

  @Override
  public InputStream certFile() {
    return this.certFile;
  }

  @Override
  public InputStream keyFile() {
    return this.keyFile;
  }

  @Override
  public ProviderSeries series() {
    return ProviderSeries.SSL;
  }

  /** Builder for SimpleSslProvider */
  public static class SslProviderBuilder {
    private final SimpleSslProvider sslProvider;

    private SslProviderBuilder(SimpleSslProvider sslProvider) {
      this.sslProvider = sslProvider;
    }

    public SslProviderBuilder certFile(String pathname) {
      if (Objects.isNull(pathname)) {
        throw new IllegalArgumentException("CertFile must not be null");
      }
      if (pathname.startsWith("/")) {
        pathname = pathname.substring(1);
      }
      this.sslProvider.certFile = this.getClass().getClassLoader().getResourceAsStream(pathname);
      return this;
    }

    public SslProviderBuilder keyFile(String pathname) {
      if (Objects.isNull(pathname)) {
        throw new IllegalArgumentException("KeyFile must not be null");
      }
      if (pathname.startsWith("/")) {
        pathname = pathname.substring(1);
      }
      this.sslProvider.keyFile = this.getClass().getClassLoader().getResourceAsStream(pathname);
      return this;
    }

    public SslProvider build() {
      if (Objects.isNull(sslProvider.certFile) || Objects.isNull(sslProvider.keyFile)) {
        throw new IllegalStateException("CertFile and keyFile are required!");
      }
      return sslProvider;
    }
  }
}
