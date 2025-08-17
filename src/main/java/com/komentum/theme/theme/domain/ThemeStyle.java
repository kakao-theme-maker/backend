package com.komentum.theme.theme.domain;

import com.komentum.theme.component.domain.ColorStyle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "theme_style")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ThemeStyleId.class)
public class ThemeStyle {

  @Id
  @Column(name = "theme_component_id")
  private Integer themeComponentId;

  @Id
  @Column(name = "color_type_id")
  private Integer colorTypeId;

  @Id
  @Column(name = "css_selector")
  private String cssSelector;

  @Id
  @Column(name = "property_name")
  private String propertyName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "theme_component_id", insertable = false, updatable = false)
  private ThemeComponent themeComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_type_id", insertable = false, updatable = false)
  private ColorStyle colorStyle;

  @Column(name = "color")
  private String color;
}
