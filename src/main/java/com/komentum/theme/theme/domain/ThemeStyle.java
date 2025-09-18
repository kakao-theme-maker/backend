package com.komentum.theme.theme.domain;

import com.komentum.theme.component.domain.ColorStyle;
import jakarta.persistence.Column;
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
@Table(name = "theme_style", uniqueConstraints = {
        @UniqueConstraint(name = "THEME_COMPONENT_COLOR_STYLE_UNIQUE", columnNames = {
                "theme_component_id", "color_style_id"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeStyle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long themeStyleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "theme_component_id")
  private ThemeComponent themeComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_style_id")
  private ColorStyle colorStyle;

  @Column(name = "color")
  private String color;
}