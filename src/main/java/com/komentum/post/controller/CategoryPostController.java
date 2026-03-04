package com.komentum.post.controller;

import com.komentum.post.dto.CategoryPostDto.CategoryPostResponse;
import com.komentum.post.facade.CategoryPostManagementFacade;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryPostController {

  private final CategoryPostManagementFacade categoryPostManagementFacade;

  /**
   * 카테고리에 게시글을 등록하고, 이미 등록되었다면 기존 상태 유지
   * */
  @PutMapping("/categories/{category_id}/posts/{post_id}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=category_id인 카테고리에 ID=post_id인 게시글을 추가한다")
  public ResponseEntity<CategoryPostResponse> registerPostOnCategory(
      @PathVariable("category_id") Long categoryId,
      @PathVariable("post_id") Long postId
  ) {
    return ResponseEntity.ok(
        categoryPostManagementFacade.registerPostOnCategory(categoryId, postId));
  }

  /**
   * 카테고리에서 특정 게시글 제거
   * */
  @DeleteMapping("/categories/{category_id}/posts/{post_id}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=category_id인 카테고리에서 ID=post_id인 게시글을 제외한다")
  public ResponseEntity<Void> deletePostFromCategory(
      @PathVariable("category_id") Long categoryId,
      @PathVariable("post_id") Long postId
  ) {
    categoryPostManagementFacade.deletePostFromCategory(categoryId, postId);
    return ResponseEntity.noContent().build();
  }
}
