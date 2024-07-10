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

import org.flmelody.netcell.core.Broker;
import org.flmelody.netcell.core.Order;
import org.flmelody.netcell.core.provider.Provider;
import org.flmelody.netcell.core.provider.delivery.MessageDeliveryProvider;
import org.flmelody.netcell.core.provider.persistence.PersistentStoreProvider;
import org.flmelody.netcell.core.provider.retained.RetainedMessageProvider;
import org.flmelody.netcell.core.provider.security.SslProvider;
import org.flmelody.netcell.core.provider.session.TemporarySessionProvider;
import org.flmelody.netcell.util.ConsoleUtil;
import org.flmelody.netcell.util.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author esotericman
 */
public class Netcell {
  private static final Logger logger = LoggerFactory.getLogger(Netcell.class);
  // Banner for Netcell
  private static final String banner =
      """
                   .__   __.  _______ .___________.  ______  _______  __       __     \s
                  |  \\ |  | |   ____||           | /      ||   ____||  |     |  |    \s
                  |   \\|  | |  |__   `---|  |----`|  ,----'|  |__   |  |     |  |    \s
                  |  . `  | |   __|      |  |     |  |     |   __|  |  |     |  |    \s
                  |  |\\   | |  |____     |  |     |  `----.|  |____ |  `----.|  `----.
                  |__| \\__| |_______|    |__|      \\______||_______||_______||_______|
                                                                                     \s
                                     Netcell Version %s""";
  // Actual broker
  private Broker broker;
  // A manager contains many useful providers
  private ProviderManager providerManager;
  // Indict whether Netcell is running or not
  private boolean isRunning;

  private Netcell() {}

  /**
   * Obtain default Netcell.
   *
   * @return Netcell
   */
  public static Netcell setup() {
    return setup(0);
  }

  /**
   * Initialize Netcell with specific port.
   *
   * @param port port you want to bind
   * @return Netcell
   */
  public static Netcell setup(int port) {
    return setup(port, false);
  }

  /**
   * Initialize Netcell with specific port and enable ssl or not,<strong>Attention, If you enable
   * ssl, you should provide a {@link SslProvider} with {@link Netcell#use(Provider...)} <strong/>.
   *
   * @see SslProvider
   * @param port port you want to bind
   * @param useSsl whether to use ssl or not
   * @return Netcell
   */
  public static Netcell setup(int port, boolean useSsl) {
    return setup(port, 0, 0, false, useSsl);
  }

  /**
   * Similar to {@link Netcell#setup(int, boolean)}, but offer a flag to use epoll in linux.
   *
   * @see SslProvider
   * @param port port you want to bind
   * @param useEpoll whether to use epoll or not in linux
   * @param useSsl whether to use ssl or not
   * @return Netcell
   */
  public static Netcell setup(int port, boolean useEpoll, boolean useSsl) {
    return setup(port, 0, 0, useEpoll, useSsl);
  }

  /**
   * Initialize Netcell with all available params.
   *
   * @see SslProvider
   * @param port port you want to bind
   * @param bossThreads boss threads for netty, you should be careful with it
   * @param workerThreads worker threads for netty, you should be careful with it
   * @param useEpoll whether to use epoll or not in linux
   * @param useSsl whether to use ssl or not
   * @return Netcell
   */
  public static Netcell setup(
      int port, int bossThreads, int workerThreads, boolean useEpoll, boolean useSsl) {
    Netcell netcell = new Netcell();
    netcell.providerManager = new ProviderManager();
    netcell.broker = new MqttBroker(port, bossThreads, workerThreads, useEpoll, useSsl);
    return netcell;
  }

  /**
   * Provide your own providers, Also, You can accomplish the same thing with the SPI. Available SPI
   * are: {@link MessageDeliveryProvider},{@link PersistentStoreProvider},{@link
   * RetainedMessageProvider}, {@link SslProvider},{@link TemporarySessionProvider}. providers are
   * also sortable for {@link Order}, the highest priority provider will be used for each type in
   * SPI.
   *
   * @param providers providers you want to use
   * @return Netcell
   */
  public Netcell use(Provider... providers) {
    if (this.isRunning) {
      logger.atWarn().log("Already running, ignoring newer providers");
      return this;
    }
    this.providerManager.use(providers);
    return this;
  }

  /** Start broker. */
  public void run() {
    System.out.println(
        ConsoleUtil.ANSI_PURPLE
            + String.format(banner, VersionUtil.NETCELL_VERSION)
            + ConsoleUtil.ANSI_RESET);
    this.broker.start();
    this.isRunning = true;
  }
}
