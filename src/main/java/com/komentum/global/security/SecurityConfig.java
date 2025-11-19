package com.komentum.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  private final SecurityProperties securityProperties;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(auth -> {
          auth.requestMatchers(securityProperties.getWhiteList()).permitAll();
          auth.requestMatchers(HttpMethod.GET, securityProperties.getWhiteListGet()).permitAll();
          auth.anyRequest().authenticated();
        });
    return http.build();
  }

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.addAllowedOriginPattern("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }
}
