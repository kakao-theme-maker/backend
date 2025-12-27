package com.komentum.global.dto;

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
public class CustomResponse<T> {
  private Integer status;
  private String message;
  private T data;

  public CustomResponse(T data){
    this.data = data;
  }

  public static <T> CustomResponse<T> ok(T data){
    return CustomResponse.<T>builder()
        .status(200)
        .message("Success")
        .data(data)
        .build();
  }

  public static <T> CustomResponse<T> error(String message){
    return CustomResponse.<T>builder()
        .status(500)
        .message(message)
        .data(null)
        .build();
  }
}