package com.komentum.theme.component.domain;

import com.komentum.theme.component.dto.ColorStyleUpdateRequest;
import com.komentum.theme.component.enums.Platform;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "color_style")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorStyle {

  @Id //PK
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "color_style_id")
  private Integer colorStyleId;

  @Column(name = "`explain`")
  private String explain;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Platform platform;

  @Column(name = "style_sheet_path", nullable = false)
  private String styleSheetPath;

  @Column(name = "style_element_name", nullable = false)
  private String styleElementName;

  @Column(name = "style_props_name", nullable = false)
  private String stylePropsName;

  public void update(ColorStyleUpdateRequest colorStyle) {
    if (colorStyle.getExplain() != null) {
      this.explain = colorStyle.getExplain();
    }
    if (colorStyle.getPlatform() != null) {
      this.platform = colorStyle.getPlatform();
    }
    if (colorStyle.getStyleSheetPath() != null) {
      this.styleSheetPath = colorStyle.getStyleSheetPath();
    }
    if (colorStyle.getStyleElementName() != null) {
      this.styleElementName = colorStyle.getStyleElementName();
    }
    if (colorStyle.getStylePropsName() != null) {
      this.stylePropsName = colorStyle.getStylePropsName();
    }
  }
  }
}
