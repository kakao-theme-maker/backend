// 복합 키를 처리하기 위해 만듦
package com.komentum.theme.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeImageId implements Serializable {

  @Column(name = "theme_component_id")
  private Integer themeComponentId;  //FK

  @Column(name = "design_component_id")
  private Integer designComponentId;
}
