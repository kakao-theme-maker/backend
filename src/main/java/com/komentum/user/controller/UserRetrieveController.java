package com.komentum.user.controller;

import com.komentum.global.dto.CustomResponse;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.dto.UserUpdateDto;
import com.komentum.user.service.UserAuthService;
import com.komentum.user.service.UserRetrieveService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserRetrieveController {

  private final UserRetrieveService userRetrieveService;
  private final UserAuthService userAuthService;

  public UserRetrieveController(UserRetrieveService userRetrieveService,
      UserAuthService userAuthService) {
    this.userRetrieveService = userRetrieveService;
    this.userAuthService = userAuthService;
  }

  // 유저 정보 조회
  @GetMapping("")
  @Operation(summary = "사용자의 공개 ID(UUID)를 기반으로 사용자 정보를 조회한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> getUserByPublicId(
      @RequestParam String userPublicID) {
    try {
      UserResponseDto user = userRetrieveService.getUserByPublicId(userPublicID);
      // 유저 조회 성공
      return ResponseEntity.ok(CustomResponse.ok(user));
    } catch (RuntimeException e) {
      return ResponseEntity.status(404)
          // 유저 조회 실패
          .body(CustomResponse.error("user not found"));
    }
  }

  // 현재 인증된 사용자 정보 조회
  @GetMapping("/me")
  @Operation(summary = "현재 인증된 사용자 정보를 조회한다")
  public ResponseEntity<UserResponseDto> retrieveCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(userRetrieveService.getUserByPublicId(userDetails.getPublicUserId()));
  }

  // 유저 정보 수정
  @PatchMapping("/me")
  @Operation(summary = "현재 사용자의 정보를 수정한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUser(
      @RequestBody UserUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserResponseDto updatedUser = userRetrieveService.updateUser(userDetails.getPublicUserId(),
        updateDto);

    return ResponseEntity.ok(
        CustomResponse.ok(updatedUser)
    );
  }

  // Local 비밀번호 변경 기능
  @PatchMapping("/me/password")
  @Operation(summary = "현재 사용자의 비밀번호를 수정한다")
  public ResponseEntity<String> changePassword(
      @RequestBody PasswordChangeRequsetDto passwordChangeRequsetDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    userAuthService.changePassword(userDetails.getUsername(), passwordChangeRequsetDto);

    return ResponseEntity.ok("success");
  }
}
