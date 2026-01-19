package com.komentum.user.controller;

import com.komentum.global.dto.CustomResponse;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.dto.UserUpdateDto;
import com.komentum.user.service.UserAuthService;
import com.komentum.user.service.UserRetrieveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
  public ResponseEntity<CustomResponse<UserResponseDto>> getUserByEmail(@RequestParam String userEmail) { // 대체키 추가 후에 수정
    try {
      UserResponseDto user = userRetrieveService.getUserByEmail(userEmail);
      // 유저 조회 성공
      return ResponseEntity.ok(CustomResponse.ok(user));
    } catch (RuntimeException e){
      return ResponseEntity.status(404)
          // 유저 조회 실패
          .body(CustomResponse.error("user not found"));
    }
  }

  // 유저 정보 수정
  @PatchMapping("/me")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUser(
      @RequestBody UserUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails userDetails){
    UserResponseDto updatedUser = userRetrieveService.updateUser(userDetails.getUsername(), updateDto);

    return ResponseEntity.ok(
        CustomResponse.ok(updatedUser)
    );
  }

  // Local 비밀번호 변경 기능
  @PatchMapping("/me/password")
  public ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequsetDto passwordChangeRequsetDto,
      @AuthenticationPrincipal CustomUserDetails userDetails){
    userAuthService.changePassword(userDetails.getUsername(), passwordChangeRequsetDto);

    return ResponseEntity.ok("success");
  }
}
