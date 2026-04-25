package com.komentum.user.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisSingleDataService {

  private final RedisTemplate<String, String> redisTemplate;

  public RedisSingleDataService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public String get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public boolean set(String key, String value, int seconds) {
    try {
      redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }

  public boolean delete(String key) {
    try {
      redisTemplate.delete(key);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }

  public Long executeLua(String script, List<String> keys, String... args) {
    try {
      DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
      return redisTemplate.execute(redisScript, keys, (Object[]) args.clone());
    } catch (Exception e) {
      log.error("Redis Lua execution error: {}", e.getMessage(), e);
      return null;
    }
  }
}
