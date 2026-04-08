package com.komentum.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 조회 응답 DTO")
public class UserResponseDto {

  @Schema(description = "사용자 이메일", example = "test@test.com")
  @JsonProperty("user_email")
  private String userEmail;

  @Schema(description = "사용자 프로필 이미지 주소", example = "https://profile-image.com")
  @JsonProperty("profile_image")
  private String profileImage;

  // 유저 조회에 필요한 정보 추가
  @Schema(description = "사용자 이름", example = "홍길동")
  private String name;

  @Schema(description = "사용자 공개 ID", example = "UUID")
  @JsonProperty("public_user_id")
  private String publicUserId;

  @JsonProperty("created_at")
  @Schema(description = "사용자 가입일", example = "YYYY-mm-ddThh:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "사용자의 팔로워 수", example = "0")
  private int followers;
  @Schema(description = "사용자의 팔로잉 수", example = "0")
  private int following;
  @Schema(description = "사용자가 업로드한 게시물 수", example = "0")
  private int uploads;
  @Schema(description = "사용자의 성별", example = "male")
  private Gender gender;
  @Schema(description = "사용자의 생년월일", example = "2000-01-01")
  private LocalDate birth;


  public static UserResponseDto from(User user, int followers, int following, int uploads,
      String profileImageUrl) {
    return UserResponseDto.builder()
        .userEmail(user.getUserEmail())
        .name(user.getName())
        .gender(user.getGender())
        .birth(user.getBirth())
        .publicUserId(user.getPublicUserId())
        .profileImage(profileImageUrl)
        .uploads(uploads)
        .followers(followers)
        .following(following)
        .createdAt(user.getCreatedAt())
        .build();
  }

}
