package com.komentum.user.controller;

import com.github.javafaker.Faker;
import com.komentum.auth.JwtUtils;
import com.komentum.global.security.UserRole;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import com.komentum.user.service.TokenService;
import java.time.LocalDate;
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

  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final JwtUtils jwtUtils;

  @PostMapping("/users/auth")
  public ResponseEntity<UserAuthResponse> getTestUserAuth() {
    User user = userRepository.findAll().get(0);
    if (user == null) {
      Faker faker = new Faker();
      user = userRepository.save(User.builder()
          .userEmail("test@test.com")
          .role(UserRole.USER)
          .birth(LocalDate.now().minusYears(10))
          .gender(Gender.male)
          .profileImg(faker.internet().image())
          .introduce(faker.lorem().word())
          .build());
    }
    String accessToken = jwtUtils.generateAccessToken(user.getUserEmail());
    String refreshToken = jwtUtils.generateRefreshToken(user.getUserEmail());
    tokenService.saveAccessAndRefreshToken(user.getUserEmail(), accessToken, refreshToken);
    return ResponseEntity.ok(new UserAuthResponse(accessToken, refreshToken));
  }
}
