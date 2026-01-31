package com.komentum.user.dto;

import com.komentum.global.security.UserRole;
import com.komentum.user.domain.User;
import java.util.UUID;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class SignUpRequestDto {
  private String email;
  private String password;

  public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder){
    String uuid = UUID.randomUUID().toString();
    return User.builder()
        .publicUserId(uuid)
        .userEmail(email)
        .encryptedPassword(bCryptPasswordEncoder.encode(password))
        .role(UserRole.USER)
        .build();
  }


}
