package com.komentum.user.client;

import com.komentum.user.dto.KakaoRequestDto.KakaoTokenRequest;
import com.komentum.user.dto.KakaoResponseDto.KakaoTokenResponse;
import com.komentum.user.dto.KakaoResponseDto.KakaoUserInfoResponse;
import com.komentum.user.dto.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KakaoAuthHttpClient {

  private final WebClient webClient;
  // 요청 url
  private final String accessTokenUrl = "https://kauth.kakao.com/oauth/token";
  private final String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
  // 카카오 accessToken 을 받아오기 위해 사용할 값들
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;

  public KakaoAuthHttpClient(
      @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String clientId,
      @Value("${spring.security.oauth2.client.registration.kakao.client-secret}") String clientSecret,
      @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}") String redirectUri
  ) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.webClient = WebClient.builder().build();
  }

  public Mono<String> getAccessToken(String code) {
    KakaoTokenRequest requestDto = KakaoTokenRequest.builder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .redirectUri(redirectUri)
        .code(code).build();
    return webClient.post().uri(accessTokenUrl)
        .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
        .bodyValue(requestDto.toMultiValueMap())
        .retrieve()
        .onStatus(HttpStatusCode::isError, res ->
            res.bodyToMono(String.class)
                .flatMap(err -> {
                  log.error("Kakao auth token error: {}", err);
                  return Mono.error(new RuntimeException(err));
                })
        )
        .bodyToMono(KakaoTokenResponse.class)
        .map(KakaoTokenResponse::getAccess_token);
  }

  public Mono<KakaoUserInfo> getUserInfo(String accessToken) {
    return webClient.get().uri(userInfoUrl)
        .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .retrieve()
        .onStatus(HttpStatusCode::isError, res ->
            res.bodyToMono(String.class)
                .flatMap(err -> {
                  log.error("Kakao user info error: {}", err);
                  return Mono.error(new RuntimeException(err));
                })
        )
        .bodyToMono(KakaoUserInfoResponse.class)
        .map(KakaoUserInfo::from);
  }

  public Mono<KakaoUserInfo> processLogin(String code) {
    return getAccessToken(code)
        .flatMap(this::getUserInfo);
  }
}
