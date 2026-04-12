package com.komentum.post.facade;

import com.komentum.post.domain.Category;
import com.komentum.post.domain.Post;
import com.komentum.post.service.CategoryPostService;
import com.komentum.post.service.CategoryService;
import com.komentum.post.service.PostService;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkManagementFacade {

  private final CategoryService categoryService;
  private final CategoryPostService categoryPostService;
  private final PostService postService;
  private final UserEntityFinder userEntityFinder;

  /**
   * 북마크에 게시글을 추가하는 API
   * - categoryType=BOOKMARK인 카테고리가 없으면 새로 생성
   * - 북마크에 이미 게시글이 존재하면, 현 상태 유지 ( 예외 X )
   * - 북마크에 게시글이 없으면, 북마크에 게시글 추가
   * */
  @Transactional
  public void addPostOnBookmark(Long postId, String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    Post targetPost = postService.getPostByPostId(postId);
    Category bookmark = categoryService.findOrCreateByCategoryTypeAndUser(
        client,
        CategoryType.BOOKMARK);
    categoryPostService.registerCategoryPost(bookmark, targetPost);
  }

  /**
   * 북마크에서 게시글을 제거하는 API
   * - categoryType=BOOKMARK인 카테고리가 없으면 현 상태 유지 ( 이미 북마크-게시글 매핑이 없는 상황이라고 판단 )
   * - 북마크에 게시글이 존재하면, 게시글을 북마크에서 제거
   * - 북마크에 게시글이 없으면, 현 상태 유지 ( 이미 북마크 - 게시글 매핑이 없는 상황이라고 판단 )
   * */
  @Transactional
  public void deletePostFromBookmark(Long postId, String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    Category bookmark = categoryService.findByCategoryTypeAndUser(client, CategoryType.BOOKMARK);
    Post targetPost = postService.getPostByPostId(postId);
    if (bookmark != null) {
      categoryPostService.deleteCategoryPost(bookmark.getCategoryId(), targetPost.getPostId());
    }
  }
}
