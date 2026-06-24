package com.komentum.theme.theme.domain;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "theme_image", uniqueConstraints = {
    @UniqueConstraint(name = "THEME_COMPONENT_COMPONENT_TYPE_UNIQUE", columnNames = {
        "theme_component_id", "component_type_id"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long themeImageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "theme_component_id")
  private ThemeComponent themeComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "design_component_id")
  private DesignComponent designComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "component_type_id")
  private ComponentType componentType;

  public static ThemeImage copyOf(ThemeComponent targetTheme, ThemeImage sourceImage) {
    return ThemeImage.builder()
        .themeComponent(targetTheme)
        .designComponent(sourceImage.getDesignComponent())
        .componentType(sourceImage.getComponentType())
        .build();
  }
}
