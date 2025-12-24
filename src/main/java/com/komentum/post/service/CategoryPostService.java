package com.komentum.post.service;

import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CategoryPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryPostService {

  private final CategoryPostRepository categoryPostRepository;

  /**
   * 특정 게시글을 카테고리에 등록
   *
   * @param category 게시글이 등록되는 카테고리 Entity
   * @param post 카테고리에 등록할 게시글 Entity
   * @return CategoryPost - 게시글:카테고리 등록 정보
   * */
  @Transactional
  public CategoryPost registerCategoryPost(Category category, Post post) {
    return categoryPostRepository.save(CategoryPost.builder()
        .category(category)
        .post(post)
        .build());
  }

  /**
   * 특정 게시글의 특정 카테고리 정보 제거
   *
   * @param categoryId 제거할 카테고리 식별자
   * @param postId 카테고리를 제거할 게시글 식별자
   * */
  @Transactional
  public void deleteCategoryPost(Long categoryId, Long postId) {
    CategoryPost categoryPost = categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        categoryId, postId).orElseThrow(
        () -> new RuntimeException("cannot find category-post by category and post id"));
    categoryPostRepository.deleteById(categoryPost.getCategoryPostId());
  }
}
