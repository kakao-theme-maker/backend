package com.komentum.post.controller;

import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryResponseDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.facade.CategoryManagementFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

  @GetMapping
  public ResponseEntity<List<CategoryResponseDto>> findAllByUser(
      @RequestParam("user_email") String userEmail) {
    return ResponseEntity.ok(categoryManagementFacade.findAllByUser(userEmail));
  }

  @PostMapping
  public ResponseEntity<CategoryResponseDto> saveCategory(
      @RequestBody CategoryCreateDto createDto) {
    return ResponseEntity.ok(categoryManagementFacade.saveCategory(createDto));
  }

  @PatchMapping("/{category_id}")
  public ResponseEntity<CategoryResponseDto> updateCategory(
      @PathVariable("category_id") Long categoryId, @RequestBody CategoryUpdateDto updateDto) {
    return ResponseEntity.ok(categoryManagementFacade.updateCategory(categoryId, updateDto));
  }

  @DeleteMapping("/{category_id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable("category_id") Long categoryId,
      @RequestParam("user_email") String userEmail) {
    categoryManagementFacade.deleteCategory(categoryId, userEmail);
    return ResponseEntity.noContent().build();
  }
}
