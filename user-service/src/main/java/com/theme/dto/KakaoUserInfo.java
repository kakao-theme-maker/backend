package com.theme.dto;

import com.theme.domain.Gender;
import com.theme.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoUserInfo {
    String email;
    String profileImage;
    String gender;
    LocalDate birth;

    public User toEntity(){
        return User.builder()
                .userEmail(email)
                .profileImg(profileImage)
                .gender(Gender.valueOf(gender))
                .birth(birth)
                .build();
    }

    private static LocalDate getUserBirth(String birthYear, String birthday) {
        String month = birthday.substring(0, 2);
        String day = birthday.substring(2, 4);
        return LocalDate.parse(birthYear + "-" + month + "-" + day);
    }

    public static KakaoUserInfo from(KakaoResponseDto.KakaoUserInfoResponse userInfo){
        String birthYear = userInfo.getKakao_account().getBirthyear(); // format : YYYY
        String birthday = userInfo.getKakao_account().getBirthday(); // format : MMDD
        return KakaoUserInfo.builder()
                .email(userInfo.getKakao_account().getEmail())
                .profileImage(userInfo.getKakao_account().getProfile().getProfile_image_url())
                .gender(userInfo.getKakao_account().getGender())
                .birth(getUserBirth(birthYear, birthday))
                .build();
    }
}
