package com.komentum.post.facade;

import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryResponseDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.service.CategoryService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserRetrieveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryManagementFacade {

  private final CategoryService categoryService;
  private final UserRetrieveService userRetrieveService;
  
  @Transactional(readOnly = true)
  public List<CategoryResponseDto> findAllByUser(String userEmail) {
    return categoryService.findAllByUser(userEmail)
        .stream().map(CategoryResponseDto::from)
        .toList();
  }

  @Transactional
  public CategoryResponseDto saveCategory(CategoryCreateDto createDto) {
    User owner = userRetrieveService.findUserEntity(createDto.getUserEmail());
    return CategoryResponseDto.from(categoryService.save(owner, createDto));
  }

  @Transactional
  public CategoryResponseDto updateCategory(long categoryId,
      CategoryUpdateDto updateDto) {
    User editor = userRetrieveService.findUserEntity(updateDto.getUserEmail());
    return CategoryResponseDto.from(categoryService.update(categoryId, editor, updateDto));
  }

  @Transactional
  public void deleteCategory(long categoryId, String editorEmail) {
    User editor = userRetrieveService.findUserEntity(editorEmail);
    categoryService.delete(categoryId, editor);
  }
}
