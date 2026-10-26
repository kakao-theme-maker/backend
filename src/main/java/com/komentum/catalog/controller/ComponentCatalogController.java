package com.komentum.catalog.controller;

import com.komentum.catalog.dto.ComponentCatalogResponse;
import com.komentum.catalog.service.ComponentCatalogService;
import com.komentum.global.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComponentCatalogController {

  private final ComponentCatalogService componentCatalogService;

  /**
   * 현재 사용자의 활성 디자인 에셋과 기존 테마를 합쳐 생성일 내림차순의 페이지로 반환한다.
   * 테마는 연결된 에셋이 비활성이어도 목록에 남긴다.
   */
  @GetMapping("/users/me/custom-components")
  @Operation(
      summary = "현재 사용자가 생성한 디자인 에셋과 테마 목록을 생성일 기준 내림차순으로 페이징 조회한다",
      description = "DESIGN은 활성 디자인 에셋만 반환합니다. "
          + "THEME은 연결된 디자인 에셋의 활성 여부와 관계없이 조회됩니다.")
  public ResponseEntity<List<ComponentCatalogResponse>> findCustomComponents(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        componentCatalogService.findComponentCatalogs(pageable, userDetails.getUsername()));
  }
}
