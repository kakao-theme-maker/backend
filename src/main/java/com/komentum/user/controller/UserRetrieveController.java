package com.komentum.user.controller;

import com.komentum.global.dto.CustomResponse;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserRequestDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.dto.UserUpdateDto;
import com.komentum.user.service.UserAuthService;
import com.komentum.user.service.UserRetrieveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  @GetMapping("/info")
  public ResponseEntity<CustomResponse<UserResponseDto>> getUserByEmail(@RequestBody UserRequestDto requestDto) {
    try {
      UserResponseDto user = userRetrieveService.getUserByEmail(requestDto.getUserEmail());
      // 유저 조회 성공
      return ResponseEntity.ok(CustomResponse.ok(user));
    } catch (RuntimeException e){
      return ResponseEntity.status(404)
          // 유저 조회 실패
          .body(CustomResponse.error("user not found"));
    }
  }

  // 유저 정보 수정
  @PutMapping("/info")
  public ResponseEntity<CustomResponse<UserResponseDto>> updateUser(
      @RequestBody UserUpdateDto updateDto, UserRequestDto requestDto){
    UserResponseDto updatedUser = userRetrieveService.updateUser(requestDto.getUserEmail(), updateDto);

    return ResponseEntity.ok(
        CustomResponse.ok(updatedUser)
    );
  }

  // Local 비밀번호 변경 기능
  @PatchMapping("/{email:.+}")
  public ResponseEntity<String> changePassword(@PathVariable("email") String email,
      @RequestBody PasswordChangeRequsetDto passwordChangeRequsetDto){
    userAuthService.changePassword(email, passwordChangeRequsetDto);

    return ResponseEntity.ok("success");
  }
}
