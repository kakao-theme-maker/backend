package com.komentum.test.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;


@Configuration
public class RedisEmbeddedConfig {

  private RedisServer redisServer;

  @PostConstruct
  public void configRedisServer() throws IOException {
    int port = getAvailablePort();

    redisServer = new RedisServer(port);
    System.setProperty("spring.data.redis.port", String.valueOf(port));
    redisServer.start();
  }

  @PreDestroy
  public void stopRedisServer() throws IOException {
    if (redisServer != null) {
      redisServer.stop();
    }
  }

  public int getAvailablePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException("No available port", e);
    }
  }

  public int getRedisPort() {
    if (redisServer == null) {
      throw new IllegalStateException("Redis server has not been initialized");
    }
    return redisServer.ports().get(0);
  }


}
