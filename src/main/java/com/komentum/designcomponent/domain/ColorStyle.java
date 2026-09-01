package com.komentum.designcomponent.domain;

import com.komentum.designcomponent.dto.ColorStyleUpdateRequest;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.StyleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "color_style")
@Getter
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

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StyleCode styleCode;

  /**
   * 이 color style이 공통으로 사용되는지, 특정 플랫폼 전용인지를 나타낸다.
   */
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(name = "platform_scope", nullable = false)
  private PlatformScope platformScope = PlatformScope.COMMON;

  public void update(ColorStyleUpdateRequest colorStyle) {
    if (colorStyle.getExplain() != null) {
      this.explain = colorStyle.getExplain();
    }
    if (colorStyle.getName() != null) {
      this.name = colorStyle.getName();
    }
    if (colorStyle.getStyleCode() != null) {
      this.styleCode = colorStyle.getStyleCode();
    }
    if (colorStyle.getPlatformScope() != null) {
      this.platformScope = colorStyle.getPlatformScope();
    }
  }

  public void replace(ColorStyle source) {
    this.explain = source.getExplain();
    this.styleCode = source.getStyleCode();
    this.name = source.getName();
    this.platformScope = source.getPlatformScope();
  }

  public boolean isSame(ColorStyle other) {
    if (other == null) {
      return false;
    }
    return Objects.equals(this.explain, other.explain)
        && this.styleCode.equals(other.styleCode)
        && this.name.equals(other.name)
        && this.platformScope == other.platformScope;
  }
}
