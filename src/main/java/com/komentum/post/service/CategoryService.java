package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Category;
import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<Category> findAllByUser(String userEmail) {
    return categoryRepository.findAllByOwner_UserEmail(userEmail);
  }

  @Transactional(readOnly = true)
  public Category findById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CustomEntityNotFoundException(Category.class, categoryId));
  }

  @Transactional
  public Category save(User owner, CategoryCreateDto createDto) {
    return categoryRepository.save(Category.createTransient(owner, createDto));
  }

  @Transactional
  public Category update(long categoryId, User editor, CategoryUpdateDto updateDto) {
    Category target = findById(categoryId);
    if (!target.isOwner(editor)) {
      throw new RuntimeException("You are not the owner of this category");
    }
    target.update(updateDto);
    return categoryRepository.save(target);
  }

  @Transactional
  public void delete(long categoryId, User editor) {
    Category target = findById(categoryId);
    if (!target.isOwner(editor)) {
      throw new RuntimeException("You are not the owner of this category");
    }
    categoryRepository.delete(target);
  }
}
