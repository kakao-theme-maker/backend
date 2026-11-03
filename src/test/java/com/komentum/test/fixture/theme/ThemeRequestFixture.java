package com.komentum.test.fixture.theme;

import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeCloneRequest;
import com.komentum.theme.core.dto.ThemeCloneRequest.ThemeImageCloneRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.InsetUpdateDto;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ThemeRequestFixture {

  private ThemeRequestFixture() {
  }

  /**
   * 이미지, 색상 변경이 없는 테마 수정 요청
   */
  public static ThemeUpdateRequest emptyUpdateRequest() {
    return ThemeUpdateRequest.builder()
        .styleCodes(Map.of())
        .build();
  }

  /**
   * 특정 TypeCode 이미지의 inset 정보만 변경하는 테마 수정 요청, 모든 inset 값을 inset으로 설정한다
   */
  public static ThemeUpdateRequest insetUpdateRequest(TypeCode typeCode, int inset) {
    return ThemeUpdateRequest.builder()
        .styleCodes(Map.of())
        .typeCodes(Map.of(typeCode, ThemeImageUpdateRequest.builder()
            .inset(new InsetUpdateDto(inset, inset, inset, inset, inset, inset))
            .build()))
        .build();
  }

  /**
   * 특정 StyleCode의 색상을 변경하는 테마 수정 요청
   */
  public static ThemeUpdateRequest styleUpdateRequest(StyleCode styleCode, String color,
      int alpha) {
    return ThemeUpdateRequest.builder()
        .styleCodes(Map.of(styleCode, ThemeStyleUpdateRequest.builder()
            .color(color).alpha(alpha).build()))
        .build();
  }

  /**
   * 특정 TypeCode 이미지들을 제거하는 테마 수정 요청
   */
  public static ThemeUpdateRequest deleteUpdateRequest(TypeCode... typeCodes) {
    return ThemeUpdateRequest.builder()
        .styleCodes(Map.of())
        .deleteTypeCodes(Set.of(typeCodes))
        .build();
  }

  /**
   * 기존 테마의 이미지/색상 정보를 그대로 복제하는 테마 복제 요청
   */
  public static ThemeCloneRequest cloneRequest(String themeName, List<ThemeImage> themeImages,
      List<ThemeStyle> themeStyles) {
    Map<TypeCode, ThemeImageCloneRequest> typeCodes = themeImages.stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            ti -> ThemeImageCloneRequest.builder()
                .designComponentId(ti.getDesignComponent().getDesignComponentId())
                .build()
        ));
    Map<StyleCode, ThemeStyleUpdateRequest> styleCodes = themeStyles.stream()
        .collect(Collectors.toMap(
            ts -> ts.getColorStyle().getStyleCode(),
            ts -> ThemeStyleUpdateRequest.builder()
                .color(ts.getColor())
                .alpha(ts.getAlpha())
                .build()
        ));
    return ThemeCloneRequest.builder()
        .themeName(themeName)
        .typeCodes(typeCodes)
        .styleCodes(styleCodes)
        .build();
  }
}
