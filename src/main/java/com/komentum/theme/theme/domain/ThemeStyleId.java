package com.komentum.theme.theme.domain;


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
public class ThemeStyleId implements Serializable {

  @Column(name = "theme_component_id")
  private Integer themeComponentId;

  @Column(name = "color_type_id")
  private Integer colorTypeId;

  @Column(name = "css_selector")
  private String cssSelector;

  @Column(name = "property_name")
  private String propertyName;
}
