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

  /**
   * user 식별자를 기반으로 category 목록 조회
   * */
  @Transactional(readOnly = true)
  public List<Category> findAllByUser(String userEmail) {
    return categoryRepository.findAllByOwner_UserEmail(userEmail);
  }

  /**
   * category 식별자를 기반으로 category 조회
   * */
  @Transactional(readOnly = true)
  public Category findById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CustomEntityNotFoundException(Category.class, categoryId));
  }

  /**
   * category 생성 정보 기반으로 category 저장
   * */
  @Transactional
  public Category save(User owner, CategoryCreateDto createDto) {
    return categoryRepository.save(Category.createTransient(owner, createDto));
  }

  /**
   * 현재 사용자가 category의 주인이라면, category 수정
   * */
  @Transactional
  public Category update(long categoryId, User editor, CategoryUpdateDto updateDto) {
    Category target = findById(categoryId);
    if (!target.isOwner(editor)) {
      throw new RuntimeException("You are not the owner of this category");
    }
    target.update(updateDto);
    return categoryRepository.save(target);
  }

  /**
   * 현재 사용자가 category의 주인이라면, category 삭제
   * */
  @Transactional
  public void delete(long categoryId, User editor) {
    Category target = findById(categoryId);
    if (!target.isOwner(editor)) {
      throw new RuntimeException("You are not the owner of this category");
    }
    categoryRepository.delete(target);
  }
}
