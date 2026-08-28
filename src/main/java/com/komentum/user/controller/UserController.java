package com.komentum.user.controller;

import com.komentum.global.dto.CustomResponse;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserBirthUpdateDto;
import com.komentum.user.dto.UserGenderUpdateDto;
import com.komentum.user.dto.UserNameUpdateDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.service.UserAuthService;
import com.komentum.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final UserAuthService userAuthService;

  public UserController(UserService userService,
      UserAuthService userAuthService) {
    this.userService = userService;
    this.userAuthService = userAuthService;
  }

  // 유저 정보 조회
  @GetMapping("")
  @Operation(summary = "사용자의 공개 ID(UUID)를 기반으로 사용자 정보를 조회한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> getUserByPublicId(
      @RequestParam String userPublicID) {
    try {
      UserResponseDto user = userService.getUserByPublicId(userPublicID);
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
    return ResponseEntity.ok(userService.getUserByPublicId(userDetails.getPublicUserId()));
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

  // 유저 이름 수정
  @PatchMapping("/me/name")
  @Operation(summary = "현재 사용자의 이름을 수정한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUserName(
      @Valid @RequestBody UserNameUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserResponseDto updatedUser = userService.updateUserName(
        userDetails.getPublicUserId(), updateDto);
    return ResponseEntity.ok(CustomResponse.ok(updatedUser));
  }

  // 유저 프로필 이미지 수정
  @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 사용자의 프로필 이미지를 수정한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUserProfileImage(
      @RequestPart("profileImage") MultipartFile profileImage,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserResponseDto updatedUser = userService.updateUserProfileImage(
        userDetails.getPublicUserId(), profileImage);
    return ResponseEntity.ok(CustomResponse.ok(updatedUser));
  }

  // 유저 성별 수정
  @PatchMapping("/me/gender")
  @Operation(summary = "현재 사용자의 성별을 수정한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUserGender(
      @Valid @RequestBody UserGenderUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserResponseDto updatedUser = userService.updateUserGender(
        userDetails.getPublicUserId(), updateDto);
    return ResponseEntity.ok(CustomResponse.ok(updatedUser));
  }

  // 유저 생년월일 수정
  @PatchMapping("/me/birth")
  @Operation(summary = "현재 사용자의 생년월일을 수정한다")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUserBirth(
      @Valid @RequestBody UserBirthUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserResponseDto updatedUser = userService.updateUserBirth(
        userDetails.getPublicUserId(), updateDto);
    return ResponseEntity.ok(CustomResponse.ok(updatedUser));
  }
}
