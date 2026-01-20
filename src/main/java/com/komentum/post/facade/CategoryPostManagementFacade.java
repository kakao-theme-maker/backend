package com.komentum.post.facade;

import com.komentum.post.domain.Category;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CategoryPostDto.CategoryPostResponse;
import com.komentum.post.service.CategoryPostService;
import com.komentum.post.service.CategoryService;
import com.komentum.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryPostManagementFacade {

  private final PostService postService;
  private final CategoryService categoryService;
  private final CategoryPostService categoryPostService;

  /**
   * 카테고리에 게시글이 있으면 유지, 없으면 추가
   * @param categoryId 카테고리 ID
   * @param postId 게시글 ID
   * */
  @Transactional
  public CategoryPostResponse registerPostOnCategory(Long categoryId, Long postId) {
    Category targetCategory = categoryService.findById(categoryId);
    Post targetPost = postService.getPostByPostId(postId);
    return CategoryPostResponse.from(
        categoryPostService.registerCategoryPost(targetCategory, targetPost));
  }

  /**
   * 카테고리에 있는 게시글 제거하고, 제거할 카테고리-게시글 정보가 없으면 예외 발생
   * @param categoryId 게시글을 제외할 카테고리 식별자
   * @param postId 카테고리에서 제외할 게시글 식별자
   * */
  @Transactional
  public void deletePostFromCategory(Long categoryId, Long postId) {
    categoryPostService.deleteCategoryPost(categoryId, postId);
  }
}
