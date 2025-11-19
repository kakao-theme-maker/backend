package com.komentum.user.dto;

import com.komentum.user.domain.User;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Data
public class LocalLoginRequestDto {
    private String email;
    private String password;

    public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder){
        return User.builder()
                .userEmail(email)
                .encryptedPassword(password)
                .build();
    }
}
