package com.komentum.global.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageableRequestDto {

  @Min(value = 0, message = "pageNumber must be greater than or equal to 0")
  private int pageNumber = 0;

  @Min(value = 1, message = "pageSize must be at least 1")
  @Max(value = 200, message = "pageSize must be at most 200")
  private int pageSize = 20;

  public Pageable toPageable() {
    return PageRequest.of(pageNumber, pageSize);
  }
}
