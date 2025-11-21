package com.komentum.test;

import com.komentum.config.EnableTestProfile;
import com.komentum.config.RedisEmbeddedConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.komentum.user.redis.RedisSingleDataService;
import com.komentum.test.RedisEmbeddedTest;
import org.springframework.context.annotation.Import;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
@SpringBootTest()
@EnableTestProfile
@Import(RedisEmbeddedConfig.class)
public class RedisEmbeddedTest {

    final String KEY = "key";
    final String VALUE = "value";
    final int SECOND = 20 ;
    @Autowired
    
    private RedisSingleDataService redisSingleDataService;

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


}


