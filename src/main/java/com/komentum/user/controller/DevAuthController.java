package com.komentum.user.controller;

import com.github.javafaker.Faker;
import com.komentum.auth.JwtUtils;
import com.komentum.global.security.UserRole;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import com.komentum.user.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevAuthController {

  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final JwtUtils jwtUtils;
  private final BCryptPasswordEncoder passwordEncoder;

  @PostMapping("/users/auth")
  @Operation(summary = "DB의 무작위 사용자를 기반으로 access token과 refresh token 발급")
  public ResponseEntity<UserAuthResponse> getTestUserAuth() {
    User user = userRepository.findByUserEmail("test@test.com").orElse(null);
    if (user == null) {
      Faker faker = new Faker();
      user = userRepository.save(User.builder()
          .userEmail("test@test.com")
          .publicUserId(UUID.randomUUID().toString())
          .encryptedPassword(passwordEncoder.encode("1234"))
          .role(UserRole.USER)
          .birth(LocalDate.now().minusYears(10))
          .gender(Gender.male)
          .profileImg(faker.internet().image())
          .introduce(faker.lorem().word())
          .build());
    }
    String accessToken = jwtUtils.generateAccessToken(user.getPublicUserId());
    String refreshToken = jwtUtils.generateRefreshToken(user.getPublicUserId());
    tokenService.saveAccessAndRefreshToken(user.getUserEmail(), accessToken, refreshToken);
    return ResponseEntity.ok(new UserAuthResponse(accessToken, refreshToken));
  }
}
