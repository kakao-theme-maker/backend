package com.komentum.user.dto;

import com.komentum.global.security.UserRole;
import com.komentum.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalLoginRequestDto {
    private String email;
    private String password;

    public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder){
        return User.builder()
                .userEmail(email)
                .encryptedPassword(bCryptPasswordEncoder.encode(password))
                .role(UserRole.USER)
                .build();
    }
}
