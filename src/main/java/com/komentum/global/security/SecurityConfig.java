package com.komentum.global.security;

import com.komentum.config.WebConfig;
import com.komentum.global.properties.FileStorageProperty;
import com.komentum.global.properties.FileStorageProperty.Storage;
import com.komentum.global.properties.SecurityProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({SecurityProperties.class, FileStorageProperty.class})
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  private final SecurityProperties securityProperties;

  private final FileStorageProperty fileStorageProperty;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .formLogin(AbstractHttpConfigurer::disable)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(securityProperties.getWhiteList()).permitAll();
          auth.requestMatchers(HttpMethod.GET, securityProperties.getWhiteListGet()).permitAll();
          // 로컬 스토리지를 사용하는 경우 업로드된 파일을 정적 리소스로 직접 서빙하므로 업로드 경로에 대한 GET 요청을 허용
          if (fileStorageProperty.getStorage() == Storage.LOCAL) {
            auth.requestMatchers(HttpMethod.GET, WebConfig.UPLOAD_URL_PREFIX + "/**").permitAll();
          }
          auth.anyRequest().authenticated();
        });
    return http.build();
  }

  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowedMethods(List.of("*"));
    configuration.setAllowedOriginPatterns(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
