package com.komentum.google.dto;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.komentum.global.security.UserRole;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserInfo {
    // 필드 정의
    String email;
    String name;
    String profileImage;


    public static GoogleUserInfo from(GoogleIdToken.Payload payload){
        //구글 api 응답을 우리 DTO로 변환

         String name = (String) payload.get("name");
         String email = payload.getEmail();
         String picture = Optional.ofNullable(payload.get("picture"))
                 .map(Object::toString)
                 .orElse("Not upload picture");

         return GoogleUserInfo.builder()
                 .email(email)
                 .name(name)
                 .profileImage(picture)
                 .build();
    }

    // toEntity() 메서드
    public User toEntity(){
        //DTO를 데이터베이스 엔티티로
        Gender gender = null;
        LocalDate birth = null;
        String introduce = null;
        return User.builder()
                .userEmail(this.email)
                .profileImg(this.profileImage)
                .role(UserRole.USER)
                .gender(gender)
                .birth(birth)
                .introduce(introduce)
                .build();
    }

    // from(GoogleIdToken.payload payload)

}
