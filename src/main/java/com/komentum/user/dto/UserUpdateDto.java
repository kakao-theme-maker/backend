package com.komentum.user.dto;

import com.komentum.user.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 수정 요청 DTO")
public class UserUpdateDto {

  @Schema(description = "새로운 사용자 이름(null 가능)", example = "홍길동")
  private String name;
  @Schema(description = "새로운 사용자 프로필 이미지 주소(null 가능)", example = "http://test.com")
  private String profileImage;
  @Schema(description = "사용자 성별(null 가능)", example = "male | female")
  private Gender gender;
  @Schema(description = "사용자 생일", example = "YYYY-mm-dd")
  private LocalDate birth;

}
