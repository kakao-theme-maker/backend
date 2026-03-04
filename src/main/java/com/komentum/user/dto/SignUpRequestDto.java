package com.komentum.user.dto;

import com.komentum.global.security.UserRole;
import com.komentum.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
@Schema(description = "로컬 회원가입 요청 DTO")
public class SignUpRequestDto {

  @Schema(description = "가입할 사용자 이메일", example = "newuser@test.com")
  private String email;
  @Schema(description = "가입할 사용자 비밀번호", example = "test1234")
  private String password;

  public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder) {
    String uuid = UUID.randomUUID().toString();
    return User.builder()
        .publicUserId(uuid)
        .userEmail(email)
        .encryptedPassword(bCryptPasswordEncoder.encode(password))
        .role(UserRole.USER)
        .build();
  }


}
