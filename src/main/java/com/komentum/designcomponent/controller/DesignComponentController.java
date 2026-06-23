package com.komentum.designcomponent.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import com.komentum.designcomponent.facade.DesignComponentFacade;
import com.komentum.designcomponent.service.DesignComponentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/design-components")
@RequiredArgsConstructor
public class DesignComponentController {

  private final DesignComponentFacade designComponentFacade;
  private final DesignComponentService designComponentService;

  /**
   * designComponent 생성
   *
   * @param request
   * @return
   */

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 DesignComponent를 생성한다.")
  public ResponseEntity<DesignComponentDto> createDesignComponent(
      @Valid @RequestPart("request") CreateDesignComponentRequest request,
      @RequestPart("image") MultipartFile image,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    DesignComponentDto saved = designComponentFacade.createDesignComponent(request, image,
        userDetails.getPublicUserId());
    return ResponseEntity.ok(saved);
  }

  @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 DesignComponent를 다중 업로드한다.")
  public ResponseEntity<List<DesignComponentDto>> createDesignComponents(
      @Valid @RequestPart("request") CreateDesignComponentRequest request,
      @RequestPart(value = "files", required = false) List<MultipartFile> files,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<DesignComponentDto> saved = designComponentFacade.createDesignComponents(request,
        files, userDetails.getPublicUserId());
    return ResponseEntity.ok(saved);
  }

  /**
   * designComponentId 로 designComponent 단건 조회
   *
   * @param designComponentId
   * @return
   */
  @GetMapping("/{id}")
  @Operation(summary = "ID = id인 designComponent를 조회한다.")
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
  @Operation(summary = "designComponent 목록을 전체 조회한다.")
  public ResponseEntity<Page<DesignComponentDto>> getAllDesignComponents(
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(designComponentService.getAllDesignComponents(pageable));
  }

  @GetMapping("/user/{publicUserId}")
  @Operation(summary = "특정 사용자의 공개 식별자로 designComponent 목록을 조회한다.")
  public ResponseEntity<List<DesignComponentDto>> getDesignComponentsByPublicUserId(
      @PathVariable("publicUserId") String publicUserId
  ) {
    return ResponseEntity.ok(designComponentService.getByPublicUserId(publicUserId));
  }

  @GetMapping("/component-types/{componentTypeId}")
  @Operation(summary = "component type ID로 designComponent 목록을 조회한다.")
  public ResponseEntity<List<DesignComponentDto>> getDesignComponentsByComponentTypeId(
      @PathVariable("componentTypeId") Integer componentTypeId
  ) {
    return ResponseEntity.ok(
        designComponentService.getDesignComponentsByComponentTypeId(componentTypeId));
  }

  @GetMapping("/bookmarked")
  @Operation(summary = "북마크한 게시글들의 design component 목록을 조회한다")
  public ResponseEntity<List<DesignComponentDto>> findBookmarkedDesignComponents(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<DesignComponentDto> res = designComponentService.findBookmarkedDesignComponents(
        userDetails.getUsername()
    );
    return ResponseEntity.ok(res);
  }

  /**
   * designComponent 수정
   *
   * @param id
   * @param request
   * @return
   */
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID = id인 designComponent를 수정한다.")
  public ResponseEntity<DesignComponentDto> updateDesignComponent(
      @PathVariable("id") Integer id,
      @RequestPart("request") @Valid UpdateDesignComponentRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {
    DesignComponentDto updated = designComponentFacade.updateDesignComponent(id, request, image);
    return ResponseEntity.ok(updated);
  }

  /**
   * designComponent 삭제
   *
   * @param id
   * @return
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID = id 인 designComponent를 삭제한다.")
  public ResponseEntity<Void> deleteDesignComponent(@PathVariable("id") Integer id) {
    designComponentFacade.deleteComponent(id);
    return ResponseEntity.noContent().build();
  }
}
