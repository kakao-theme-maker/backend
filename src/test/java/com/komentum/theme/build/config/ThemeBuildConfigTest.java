package com.komentum.theme.build.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class ThemeBuildConfigTest {

  @Test
  @DisplayName("테마 build worker에 호출자 인증을 전달하고 작업 후 제거한다")
  void themeBuildExecutor_propagatesAndClearsSecurityContext() throws InterruptedException {
    ThreadPoolTaskExecutor executor =
        (ThreadPoolTaskExecutor) new ThemeBuildConfig().themeBuildExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.initialize();
    executor.getThreadPoolExecutor().prestartCoreThread();

    Authentication callerAuthentication = new UsernamePasswordAuthenticationToken(
        "caller",
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
    AtomicReference<Authentication> propagatedAuthentication = new AtomicReference<>();
    AtomicReference<Authentication> remainingAuthentication = new AtomicReference<>();
    CountDownLatch propagated = new CountDownLatch(1);
    CountDownLatch cleaned = new CountDownLatch(1);

    try {
      SecurityContextHolder.getContext().setAuthentication(callerAuthentication);
      executor.execute(() -> {
        propagatedAuthentication.set(SecurityContextHolder.getContext().getAuthentication());
        propagated.countDown();
      });

      assertThat(propagated.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(propagatedAuthentication.get()).isSameAs(callerAuthentication);

      Runnable cleanupProbe = () -> {
        remainingAuthentication.set(SecurityContextHolder.getContext().getAuthentication());
        cleaned.countDown();
      };
      assertThat(executor.getThreadPoolExecutor().getQueue().offer(cleanupProbe)).isTrue();

      assertThat(cleaned.await(5, TimeUnit.SECONDS)).isTrue();
      assertThat(remainingAuthentication.get()).isNull();
    } finally {
      SecurityContextHolder.clearContext();
      executor.shutdown();
    }
  }
}
