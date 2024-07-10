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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.ssl.OptionalSslHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.Objects;
import java.util.function.Supplier;
import javax.net.ssl.SSLException;
import org.flmelody.netcell.core.Broker;
import org.flmelody.netcell.core.handler.MqttMessageHandler;
import org.flmelody.netcell.core.provider.ProviderSeries;
import org.flmelody.netcell.core.provider.security.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author esotericman
 */
public class MqttBroker implements Broker {
  private static final Logger logger = LoggerFactory.getLogger(MqttBroker.class);
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private final int port;
  private final boolean useEpoll;
  private final boolean useSsl;

  public MqttBroker() {
    this(0);
  }

  public MqttBroker(int port) {
    this(port, 0, 0, false, false);
  }

  public MqttBroker(int port, boolean useSsl) {
    this(port, 0, 0, false, useSsl);
  }

  public MqttBroker(int port, boolean useEpoll, boolean useSsl) {
    this(port, 0, 0, useEpoll, useSsl);
  }

  public MqttBroker(
      int port, int bossThreads, int workerThreads, boolean useEpoll, boolean useSsl) {
    if (useEpoll) {
      this.bossGroup = new EpollEventLoopGroup(bossThreads);
      this.workerGroup = new EpollEventLoopGroup(workerThreads);
    } else {
      this.bossGroup = new NioEventLoopGroup(bossThreads);
      this.workerGroup = new NioEventLoopGroup(workerThreads);
    }
    this.useEpoll = useEpoll;
    this.useSsl = useSsl;
    if (port == 0) {
      if (this.useSsl) {
        this.port = 8883;
      } else {
        this.port = 1883;
      }
    } else {
      this.port = port;
    }
  }

  @Override
  public void start() {
    try {
      ServerBootstrap bootstrap =
          new ServerBootstrap()
              .group(bossGroup, workerGroup)
              .channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
              .childHandler(new BrokerChannelInitializer(initializeSslContext()))
              .childOption(ChannelOption.TCP_NODELAY, true)
              .childOption(ChannelOption.SO_KEEPALIVE, true)
              .option(ChannelOption.SO_REUSEADDR, true);
      ChannelFuture f = bootstrap.bind(this.port).sync();
      logger.atInfo().log("Netcell started successfully, listening on port {}", port);
      f.channel().closeFuture().sync();
    } catch (Exception e) {
      logger.error("Netcell started failed", e);
    } finally {
      this.bossGroup.shutdownGracefully();
      this.workerGroup.shutdownGracefully();
    }
  }

  private SslContext initializeSslContext() {
    if (useSsl) {
      if (Objects.isNull(ProviderManager.provider(ProviderSeries.SSL))) {
        logger.atWarn().log("SSL provider not set, ignoring ssl setting");
      } else {
        try {
          return SslContextBuilder.forServer(
                  ProviderManager.provider(
                          ProviderSeries.SSL, SslProvider.class, (Supplier<SslProvider>) () -> null)
                      .certFile(),
                  ProviderManager.provider(
                          ProviderSeries.SSL, SslProvider.class, (Supplier<SslProvider>) () -> null)
                      .keyFile())
              .build();
        } catch (SSLException ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    return null;
  }

  /** Initialize channel */
  static class BrokerChannelInitializer extends ChannelInitializer<Channel> {
    private final SslContext sslContext;

    BrokerChannelInitializer(SslContext sslContext) {
      this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();
      if (Objects.nonNull(sslContext)) {
        pipeline.addLast(new OptionalSslHandler(sslContext));
      }
      pipeline.addLast(new MqttDecoder());
      pipeline.addLast(MqttEncoder.INSTANCE);
      pipeline.addLast(new IdleStateHandler(0, 0, 10));
      pipeline.addLast(new MqttMessageHandler(new MqttDispatcher().assembleListeners()));
    }
  }
}
