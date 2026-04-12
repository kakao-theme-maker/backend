package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.policy.CategoryPolicy;
import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryPolicy categoryPolicy;
  private final CategoryRepository categoryRepository;

  /**
   * user 식별자를 기반으로 category 목록 조회
   * */
  @Transactional(readOnly = true)
  public List<Category> findAllByUser(String userEmail) {
    return categoryRepository.findAllByOwner_UserEmail(userEmail);
  }

  /**
   * 카테고리 타입과 사용자를 기반으로 카테고리 조회
   * @param client 카테고리 소유자
   * @param categoryType 조회할 카테고리 타입
   * */
  @Transactional
  public Category findByCategoryTypeAndUser(User client, CategoryType categoryType) {
    return categoryRepository.findByCategoryTypeAndOwner(categoryType, client)
        .orElse(null);
  }

  /**
   * 카테고리 타입과 사용자를 기반으로 카테고리를 조회하고, 없으면 새로 생성
   * @param client 카테고리 소유자
   * @param categoryType 조회 및 생성할 카테고리 타입
   * */
  @Transactional
  public Category findOrCreateByCategoryTypeAndUser(User client, CategoryType categoryType) {
    Category targetCategory = findByCategoryTypeAndUser(client, categoryType);
    if (targetCategory == null) {
      targetCategory = categoryRepository.save(Category.builder()
          .categoryType(categoryType)
          .owner(client)
          .name(categoryType.name() + client.getPublicUserId())
          .build());
    }
    return targetCategory;
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
    if (!categoryPolicy.canUpdate(target.getOwner())) {
      throw new AccessDeniedException("failed to update category : invalid user or role");
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
    if (!categoryPolicy.canDelete(target.getOwner())) {
      throw new AccessDeniedException("failed to delete category : invalid user or role");
    }
    categoryRepository.delete(target);
  }
}
