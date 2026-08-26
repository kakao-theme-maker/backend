package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryResponseDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.facade.CategoryManagementFacade;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryManagementFacade categoryManagementFacade;

  /**
   * 특정 사용자가 생성한 모든 카테고리 조회
   * */
  @GetMapping
  @Operation(summary = "이메일=userEmail인 사용자가 소유한 모든 카테고리를 조회한다")
  public ResponseEntity<List<CategoryResponseDto>> findAllByUser(
      @RequestParam("userEmail") String userEmail) {
    return ResponseEntity.ok(categoryManagementFacade.findAllByUser(userEmail));
  }

  /**
   * 특정 사용자가 새로운 카테고리 생성
   * */
  @PostMapping
  @Operation(summary = "현재 인증된 사용자가 카테고리를 생성한다")
  public ResponseEntity<CategoryResponseDto> saveCategory(
      @RequestBody CategoryCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        categoryManagementFacade.saveCategory(createDto, userDetails.getUsername()));
  }

  /**
   * 특정 사용자가 생성한 카테고리 수정
   * */
  @PatchMapping("/{categoryId}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=categoryId인 카테고리를 수정한다")
  public ResponseEntity<CategoryResponseDto> updateCategory(
      @PathVariable Long categoryId, @RequestBody CategoryUpdateDto updateDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        categoryManagementFacade.updateCategory(categoryId, updateDto, userDetails.getUsername()));
  }

  /**
   * 특정 사용자가 생성한 카테고리 삭제
   * */
  @DeleteMapping("/{categoryId}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=categoryId인 카테고리를 삭제한다")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    categoryManagementFacade.deleteCategory(categoryId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }
}
