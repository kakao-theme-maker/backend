package com.komentum.catalog.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentSummary {

  private Integer id;
  private ComponentType type;
  private String previewImageUrl;
  private LocalDateTime createdAt;
}
