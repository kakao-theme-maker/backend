package com.komentum.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.komentum.config.RedisEmbeddedConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisEmbeddedPortTest {

  @Autowired
  private RedisEmbeddedConfig redisEmbeddedConfig;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Test
  void testEmbeddedRedisIsRunning() {
    int port = redisEmbeddedConfig.getRedisPort();
    System.out.println("Embedded Redis Port = " + port);

    // ping 테스트
    String pong = redisTemplate.getConnectionFactory().getConnection().ping();
    System.out.println("PING: " + pong);

    assertEquals("PONG", pong);
  }
}