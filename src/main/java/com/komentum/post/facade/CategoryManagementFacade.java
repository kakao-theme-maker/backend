package com.komentum.post.facade;

import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryResponseDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.service.CategoryService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryManagementFacade {

  private final CategoryService categoryService;
  private final UserService userService;

  /**
   * 특정 사용자가 생성한 모든 카테고리 조회
   *
   * @param userEmail 사용자 이메일
   * @return 카테고리 정보 목록
   */
  @Transactional(readOnly = true)
  public List<CategoryResponseDto> findAllByUser(String userEmail) {
    return categoryService.findAllByUser(userEmail)
        .stream().map(CategoryResponseDto::from)
        .toList();
  }

  /**
   * 특정 사용자가 카테고리 저장
   *
   * @param createDto 카테고리 생성 정보
   * @param authorId  카테고리 생성한 사용자의 식별자
   * @return 생성된 카테고리 정보
   */
  @Transactional
  public CategoryResponseDto saveCategory(CategoryCreateDto createDto, String authorId) {
    User owner = userService.findUserEntity(authorId);
    return CategoryResponseDto.from(categoryService.save(owner, createDto));
  }

  /**
   * 특정 사용자가 카테고리 수정
   *
   * @param categoryId 수정할 카테고리 식별자
   * @param updateDto  카테고리 수정 정보
   * @param editorId   카테고리 수정한 사용자의 식별자
   * @return 수정된 카테고리 정보
   */
  @Transactional
  public CategoryResponseDto updateCategory(Long categoryId,
      CategoryUpdateDto updateDto, String editorId) {
    User editor = userService.findUserEntity(editorId);
    return CategoryResponseDto.from(categoryService.update(categoryId, editor, updateDto));
  }

  /**
   * 특정 사용자가 카테고리 삭제
   *
   * @param categoryId  삭제할 카테고리 식별자
   * @param editorEmail 카테고리 삭제할 사용자의 식별자
   */
  @Transactional
  public void deleteCategory(long categoryId, String editorEmail) {
    User editor = userService.findUserEntity(editorEmail);
    categoryService.delete(categoryId, editor);
  }
}
