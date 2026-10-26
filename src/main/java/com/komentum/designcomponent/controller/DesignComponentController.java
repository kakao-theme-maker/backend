package com.komentum.designcomponent.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import com.komentum.designcomponent.facade.DesignComponentFacade;
import com.komentum.designcomponent.service.DesignComponentService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
   * ID에 해당하는 활성 컴포넌트 응답을 반환한다.
   * 비활성 컴포넌트는 소유자와 관리자에게도 조회되지 않는다.
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "ID = id인 활성 designComponent를 조회한다.",
      description = "삭제되어 비활성 상태인 컴포넌트는 소유자와 관리자도 직접 조회할 수 없으며 404를 반환한다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "활성 컴포넌트 조회 성공"),
      @ApiResponse(responseCode = "404", description = "컴포넌트가 존재하지 않거나 비활성 상태")
  })
  public ResponseEntity<DesignComponentDto> getDesignComponentById(
      @PathVariable("id") Integer designComponentId) {
    return ResponseEntity.ok(designComponentFacade.getDesignComponentById(designComponentId));
  }

  /**
   * 활성 컴포넌트만을 대상으로 계산한 페이지 내용과 전체 개수를 반환한다.
   */
  @GetMapping
  @Operation(
      summary = "designComponent 목록을 전체 조회한다.",
      description = "비활성 컴포넌트를 제외하고 페이징하며 전체 개수도 활성 컴포넌트 기준으로 계산한다.")
  public ResponseEntity<Page<DesignComponentDto>> getAllDesignComponents(
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(designComponentService.getAllDesignComponents(pageable));
  }

  /**
   * 공개 식별자로 지정한 사용자의 활성 컴포넌트 목록을 반환한다.
   */
  @GetMapping("/user/{publicUserId}")
  @Operation(
      summary = "특정 사용자의 공개 식별자로 designComponent 목록을 조회한다.",
      description = "비활성 컴포넌트를 제외하고 활성 컴포넌트만 반환한다.")
  public ResponseEntity<List<DesignComponentDto>> getDesignComponentsByPublicUserId(
      @PathVariable("publicUserId") String publicUserId
  ) {
    return ResponseEntity.ok(designComponentService.getByPublicUserId(publicUserId));
  }

  /**
   * 지정한 타입에 연결된 활성 컴포넌트 목록을 반환한다.
   */
  @GetMapping("/component-types/{componentTypeId}")
  @Operation(
      summary = "component type ID로 designComponent 목록을 조회한다.",
      description = "비활성 컴포넌트를 제외하고 활성 컴포넌트만 반환한다.")
  public ResponseEntity<List<DesignComponentDto>> getDesignComponentsByComponentTypeId(
      @PathVariable("componentTypeId") Integer componentTypeId
  ) {
    return ResponseEntity.ok(
        designComponentService.getDesignComponentsByComponentTypeId(componentTypeId));
  }

  /**
   * 현재 사용자가 업로드한 컴포넌트 중 활성 항목만 반환한다.
   */
  @GetMapping("/uploaded")
  @Operation(
      summary = "업로드한 design component 목록을 조회한다",
      description = "비활성 컴포넌트를 제외하고 활성 컴포넌트만 반환한다.")
  public ResponseEntity<List<DesignComponentDto>> findUploadedDesignComponents(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<DesignComponentDto> res = designComponentFacade.findUploadedDesignComponents(
        userDetails.getUsername()
    );
    return ResponseEntity.ok(res);
  }

  /**
   * 현재 사용자의 북마크 게시글에 연결된 활성 컴포넌트 목록을 반환한다.
   */
  @Hidden
  @GetMapping("/bookmarked")
  @Operation(
      summary = "북마크한 게시글들의 design component 목록을 조회한다",
      description = "비활성 컴포넌트를 제외하고 활성 컴포넌트만 반환한다.")
  public ResponseEntity<List<DesignComponentDto>> findBookmarkedDesignComponents(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<DesignComponentDto> res = designComponentService.findBookmarkedDesignComponents(
        userDetails.getUsername()
    );
    return ResponseEntity.ok(res);
  }

  /**
   * 소유자·관리자 권한 검사와 활성 검증을 통과한 컴포넌트의 수정 결과를 반환한다.
   */
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "소유자 또는 관리자가 ID = id인 활성 designComponent를 수정한다.",
      description = """
          존재하지 않는 ID는 404를 반환한다. 존재하는 컴포넌트는 소유자·관리자 권한을 먼저 검사한다.
          권한이 없으면 403, 권한이 있어도 비활성 컴포넌트 수정은 404를 반환한다.
          """)
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "활성 컴포넌트 수정 성공"),
      @ApiResponse(responseCode = "403", description = "소유자 또는 관리자 권한 없음"),
      @ApiResponse(responseCode = "404", description = "컴포넌트가 존재하지 않거나 권한 검사 후 비활성 상태로 확인됨")
  })
  public ResponseEntity<DesignComponentDto> updateDesignComponent(
      @PathVariable("id") Integer id,
      @RequestPart("request") @Valid UpdateDesignComponentRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image) {
    DesignComponentDto updated = designComponentFacade.updateDesignComponent(id, request, image);
    return ResponseEntity.ok(updated);
  }

  /**
   * 소유자 또는 관리자의 비활성화 요청을 처리하고 본문 없는 204 응답을 반환한다.
   * 이미 비활성인 경우에도 권한 검사를 통과하면 같은 응답을 반환한다.
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "소유자 또는 관리자가 ID = id인 designComponent를 비활성화한다.",
      description = """
          기존 테마의 참조를 유지하도록 DB 행과 이미지 파일을 보존하고 컴포넌트를 비활성화한다.
          이미 비활성인 컴포넌트의 재삭제도 권한 검사 후 204를 반환하며 응답 본문은 없다.
          비활성 상태에서도 권한이 없으면 403, 존재하지 않는 ID는 404를 반환한다.
          """)
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "비활성화 또는 권한 있는 재삭제 성공", content = @Content),
      @ApiResponse(responseCode = "403", description = "소유자 또는 관리자 권한 없음"),
      @ApiResponse(responseCode = "404", description = "컴포넌트가 존재하지 않음")
  })
  public ResponseEntity<Void> deleteDesignComponent(@PathVariable("id") Integer id) {
    designComponentFacade.deleteComponent(id);
    return ResponseEntity.noContent().build();
  }
}
