package com.komentum.theme.component.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.facade.DesignComponentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/design-components")
@RequiredArgsConstructor
public class DesignComponentController {

  private final DesignComponentFacade designComponentFacade;

  /**
   * designComponent 생성
   *
   * @param request
   * @return
   */
  @PostMapping
  public ResponseEntity<DesignComponentDto> createDesignComponent(
      @Valid @RequestBody CreateDesignComponentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    DesignComponentDto saved = designComponentFacade.createDesignComponent(request,
        userDetails.getPublicUserId());
    return ResponseEntity.ok(saved);
  }

  /**
   * designComponentId 로 designComponent 단건 조회
   *
   * @param designComponentId
   * @return
   */
  @GetMapping("/{id}")
  public ResponseEntity<DesignComponentDto> getDesignComponentById(
      @PathVariable("id") Integer designComponentId) {
    return ResponseEntity.ok(designComponentFacade.getDesignComponentById(designComponentId));
  }

  /**
   * designComponent 전체 조회
   *
   * @param pageable
   * @return
   */
  @GetMapping
  public ResponseEntity<Page<DesignComponentDto>> getAllDesignComponents(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(designComponentFacade.getAllDesignComponents(pageable));
  }


  /**
   * designComponent 수정
   *
   * @param id
   * @param request
   * @return
   */
  @PutMapping("/{id}")
  public ResponseEntity<DesignComponentDto> updateDesignComponent(
      @PathVariable("id") Integer id,
      @Valid @RequestBody UpdateDesignComponentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    DesignComponentDto updated = designComponentFacade.updateDesignComponent(id, request,
        userDetails.getPublicUserId());
    return ResponseEntity.ok(updated);
  }

  /**
   * designComponent 삭제
   *
   * @param id
   * @return
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDesignComponent(@PathVariable("id") Integer id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    designComponentFacade.deleteComponent(id, userDetails.getPublicUserId());
    return ResponseEntity.noContent().build();
  }
}