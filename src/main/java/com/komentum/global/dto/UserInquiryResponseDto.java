package com.komentum.global.dto;

import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserRetrieveService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 커스텀 응답 객체 DTO
public class UserInquiryResponseDto<T> {
  private boolean status;
  private String message;
  private T data;

  public UserInquiryResponseDto(T data){
    this.data = data;
  }

  public static <T> UserInquiryResponseDto<T> ok(T data){
    return UserInquiryResponseDto.<T>builder()
        .status(true)
        .message("Success")
        .data(data)
        .build();
  }

  public static <T> UserInquiryResponseDto<T> error(String message){
    return UserInquiryResponseDto.<T>builder()
        .status(false)
        .message(message)
        .data(null)
        .build();
  }
}
