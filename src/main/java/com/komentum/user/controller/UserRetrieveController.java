package com.komentum.user.controller;

import com.komentum.global.dto.UserInquiryResponseDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.service.UserRetrieveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserRetrieveController {

  private final UserRetrieveService userRetrieveService;
  public UserRetrieveController(UserRetrieveService userRetrieveService) {
    this.userRetrieveService = userRetrieveService;
  }

  // 유저 조회
  @GetMapping("/{email:.+}")
  public ResponseEntity<UserInquiryResponseDto<UserResponseDto>> getUserByEmail(@PathVariable("email") String email) {
    try {
      UserResponseDto user = userRetrieveService.getUserByEmail(email);
      // 유저 조회 성공
      return ResponseEntity.ok(UserInquiryResponseDto.ok(user));
    } catch (RuntimeException e){
      return ResponseEntity.status(404)
          // 유저 조회 실패
          .body(UserInquiryResponseDto.error("user not found"));
    }
  }
}
