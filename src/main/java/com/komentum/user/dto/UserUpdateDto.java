package com.komentum.user.dto;

import com.komentum.user.domain.Gender;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
  private String name;
  private String profileImage;
  private Gender gender;
  private LocalDate birth;

}
