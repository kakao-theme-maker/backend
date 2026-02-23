package com.komentum.test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.config.RedisEmbeddedConfig;
import com.komentum.user.redis.RedisSingleDataService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@SpringBootTest()
@EnableTestProfile
@Import(RedisEmbeddedConfig.class)
public class RedisEmbeddedTest {

  final String KEY = "key";
  final String VALUE = "value";
  final int SECOND = 20;
  @Autowired

  private RedisSingleDataService redisSingleDataService;

  @Autowired
  private RedisEmbeddedConfig redisEmbeddedConfig;

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @BeforeEach
  void shutDown() {
    redisSingleDataService.set(KEY, VALUE, SECOND);
  }

  @AfterEach
  void tearDown() {
    redisSingleDataService.delete(KEY);
  }

  @Test
  @DisplayName("Redis에 데이터를 저장하면 정상적으로 조회된다.")
  void saveAndFindTest() throws Exception {
    // when
    String findValue = redisSingleDataService.get(KEY);

    // then
    assertThat(VALUE).isEqualTo(findValue);
  }

  @Test
  @DisplayName("Redis에 저장된 데이터를 수정할 수 있다.")
  void updateTest() throws Exception {
    // given
    String updateValue = "updatevalue";
    redisSingleDataService.set(KEY, updateValue, SECOND);

    // when
    String findValue = redisSingleDataService.get(KEY);

    // then
    assertThat(updateValue).isEqualTo(findValue);
    assertThat(VALUE).isNotEqualTo(findValue);
  }

  @Test
  @DisplayName("Redis에 저장된 데이터를 삭제할 수 있다.")
  void deleteTest() throws Exception {
    // when
    redisSingleDataService.delete(KEY);
    String findValue = redisSingleDataService.get(KEY);

    // then
    assertThat(redisSingleDataService.get(KEY)).isNull();
  }

  @Test
  @DisplayName("Redis port 테스트")
  void testEmbeddedRedisIsRunning() {
    int port = redisEmbeddedConfig.getRedisPort();
    System.out.println("Embedded Redis Port = " + port);

    // ping 테스트
    String pong = redisTemplate.getConnectionFactory().getConnection().ping();
    System.out.println("PING: " + pong);

    assertEquals("PONG", pong);
  }


}


