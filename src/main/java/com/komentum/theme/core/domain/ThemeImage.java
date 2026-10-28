package com.komentum.theme.core.domain;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.theme.core.dto.ThemeUpdateRequest.InsetUpdateDto;
import jakarta.persistence.Embedded;
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

  // inset 정보가 필요한 이미지만 imageInset을 가져도 됨
  @Embedded
  private ImageInset imageInset;

  /**
   * 원본의 컴포넌트와 타입을 참조하는 새 테마 이미지 연결을 생성한다.
   * 에셋과 이미지 파일은 공유하며, 새 연결에 사용할 컴포넌트의 활성 검증은 호출 측에서 수행한다.
   */
  public static ThemeImage copyOf(ThemeComponent targetTheme, ThemeImage sourceImage) {
    return ThemeImage.builder()
        .themeComponent(targetTheme)
        .designComponent(sourceImage.getDesignComponent())
        .componentType(sourceImage.getComponentType())
        .build();
  }

  /**
   * 이미지가 참조하는 designComponent와 inset 정보를 갱신한다. null인 값은 기존 정보를 유지한다.
   */
  public void update(DesignComponent designComponent, InsetUpdateDto inset) {
    if (designComponent != null) {
      this.designComponent = designComponent;
    }
    if (inset != null) {
      this.imageInset = ImageInset.from(inset);
    }
  }
}
