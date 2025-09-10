package com.komentum.theme.component.domain;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // JPA 엔티티 선언
@Table(name = "color_style") //color_style과 매핑
@Data
@Builder // 빌더 패턴 사용하여 객체 생성할 수 있도록 함.
@NoArgsConstructor // 어떠한 변수도 사용하지 않는 기본 생성자를 자동완성 시켜주는 어노테이션
@AllArgsConstructor // 모든 필드를 초기화하는 생성자를 자동완성 시켜주는 어노테이션
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

  public void update(ColorStyle colorStyle) {
    this.explain = colorStyle.explain;
    this.platform = colorStyle.platform;
    this.styleSheetPath = colorStyle.styleSheetPath;
    this.styleElementName = colorStyle.styleElementName;
    this.stylePropsName = colorStyle.stylePropsName;
  }
}
