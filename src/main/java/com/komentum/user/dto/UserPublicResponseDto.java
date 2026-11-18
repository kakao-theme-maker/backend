package com.komentum.user.dto;

import com.komentum.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "다른 사용자 정보 조회 응답 DTO (공개 프로필 정보만 포함)")
public class UserPublicResponseDto {

  @Schema(description = "사용자 프로필 이미지 주소", example = "https://profile-image.com")
  private String profileImage;

  @Schema(description = "사용자 프로필 이미지 파일명", example = "profile-image.png")
  private String profileImageName;

  @Schema(description = "사용자 이름", example = "홍길동")
  private String name;

  @Schema(description = "사용자 한줄소개", example = "안녕하세요.\n반갑습니다.")
  private String introduce;

  @Schema(description = "사용자 공개 ID", example = "UUID")
  private String publicUserId;

  @Schema(description = "사용자 가입일", example = "YYYY-mm-ddThh:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "사용자의 팔로워 수", example = "0")
  private int followers;
  @Schema(description = "사용자의 팔로잉 수", example = "0")
  private int following;
  @Schema(description = "사용자가 업로드한 게시물 수", example = "0")
  private int uploads;

  /**
   * 사용자 엔티티와 집계값으로 공개 프로필 응답을 생성한다. 이메일, 성별, 생년월일, 권한 등 본인 전용 정보는 포함하지 않는다.
   */
  public static UserPublicResponseDto from(User user, int followers, int following, int uploads,
      String profileImageUrl) {
    return UserPublicResponseDto.builder()
        .name(user.getName())
        .introduce(user.getIntroduce())
        .publicUserId(user.getPublicUserId())
        .profileImage(profileImageUrl)
        .profileImageName(user.getProfileImgName())
        .uploads(uploads)
        .followers(followers)
        .following(following)
        .createdAt(user.getCreatedAt())
        .build();
  }
}
