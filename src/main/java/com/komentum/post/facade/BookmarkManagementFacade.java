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

  @Transactional
  public void addPostOnBookmark(Long postId, String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    Post targetPost = postService.getPostByPostId(postId);
    Category bookmark = categoryService.findOrCreateByCategoryTypeAndUser(
        client,
        CategoryType.BOOKMARK);
    categoryPostService.registerCategoryPost(bookmark, targetPost);
  }

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
