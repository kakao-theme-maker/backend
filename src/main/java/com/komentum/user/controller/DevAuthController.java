package com.komentum.user.controller;

import com.komentum.auth.JwtUtils;
import com.komentum.seed.seeder.UserSeeder;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevAuthController {

  private final UserSeeder userSeeder;
  private final TokenService tokenService;
  private final JwtUtils jwtUtils;

  @PostMapping("/users/auth")
  @Operation(summary = "DB의 무작위 사용자를 기반으로 access token과 refresh token 발급")
  public ResponseEntity<UserAuthResponse> getTestUserAuth() {
    User user = userSeeder.createOrRetrieveRootUser();
    String accessToken = jwtUtils.generateAccessToken(user.getPublicUserId());
    String refreshToken = jwtUtils.generateRefreshToken(user.getPublicUserId());
    tokenService.saveAccessAndRefreshToken(user.getUserEmail(), accessToken, refreshToken);
    return ResponseEntity.ok(new UserAuthResponse(accessToken, refreshToken));
  }
}
