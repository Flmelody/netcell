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

package org.flmelody.core.netty;

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
import org.flmelody.core.Broker;
import org.flmelody.core.netty.handler.MqttMessageHandler;
import org.flmelody.core.spi.StoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

/**
 * @author esotericman
 */
public class MqttBroker implements Broker {
  private static final Logger logger = LoggerFactory.getLogger(MqttBroker.class);
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private final int port;
  private final boolean useEpoll;
  private final boolean useSSL;
  private StoreProvider storeProvider;

  public MqttBroker() {
    this(0);
  }

  public MqttBroker(int port) {
    this(port, 0, 0, false, false);
  }

  public MqttBroker(int port, boolean useSSL) {
    this(port, 0, 0, false, useSSL);
  }

  public MqttBroker(int port, boolean useEpoll, boolean useSSL) {
    this(port, 0, 0, useEpoll, useSSL);
  }

  public MqttBroker(
      int port, int bossThreads, int workerThreads, boolean useEpoll, boolean useSSL) {
    if (useEpoll) {
      this.bossGroup = new EpollEventLoopGroup(bossThreads);
      this.workerGroup = new EpollEventLoopGroup(workerThreads);
    } else {
      this.bossGroup = new NioEventLoopGroup(bossThreads);
      this.workerGroup = new NioEventLoopGroup(workerThreads);
    }
    this.useEpoll = useEpoll;
    this.useSSL = useSSL;
    if (port == 0) {
      if (this.useSSL) {
        this.port = 8883;
      } else {
        this.port = 1883;
      }
    } else {
      this.port = port;
    }
    loadStore();
  }

  private void loadStore() {
    storeProvider = ServiceLoader.load(StoreProvider.class).findFirst().orElse(null);
  }

  @Override
  public void start() throws Exception {
    try {
      ServerBootstrap bootstrap =
          new ServerBootstrap()
              .group(bossGroup, workerGroup)
              .channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
              .childHandler(
                  new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                      ChannelPipeline pipeline = ch.pipeline();
                      pipeline.addLast(new MqttDecoder());
                      pipeline.addLast(MqttEncoder.INSTANCE);
                      pipeline.addLast(new MqttMessageHandler());
                    }
                  })
              .childOption(ChannelOption.TCP_NODELAY, true)
              .childOption(ChannelOption.SO_KEEPALIVE, true)
              .option(ChannelOption.SO_REUSEADDR, true);
      ChannelFuture f = bootstrap.bind(this.port).sync();
      logger.atInfo().log("Mqtt broker started successfully, listening on port {}", port);
      f.channel().closeFuture().sync();
    } finally {
      this.bossGroup.shutdownGracefully();
      this.workerGroup.shutdownGracefully();
    }
  }
}
