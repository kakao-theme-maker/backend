package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.google.common.base.Functions;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BookmarkSeeder {

  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final CategoryPostRepository categoryPostRepository;
  private final Faker faker;

  public Category createBookmark(User user) {
    return Category.builder()
        .name(faker.animal().name())
        .owner(user)
        .categoryType(CategoryType.BOOKMARK)
        .build();
  }

  public CategoryPost createBookmarkPost(Category category, Post post) {
    return CategoryPost.builder()
        .post(post)
        .category(category)
        .build();
  }

  @Transactional
  public List<Category> bookmarkByRatio(List<User> users, List<Post> posts, double ratio) {
    if (ratio < 0 || ratio > 1) {
      throw new IllegalArgumentException("BookmarkSeeder : ratio must be between 0 and 1");
    }
    // make random posts list with a target ratio
    List<Post> shuffledPosts = new ArrayList<>(posts);
    Collections.shuffle(shuffledPosts);
    List<Post> selectedPosts = shuffledPosts.subList(0, (int) (posts.size() * ratio));
    // create a user:bookmark map
    Map<User, Category> userBookmarkMap = categoryRepository
        .fetchJoinAllByCategoryTypeAndOwnerIn(CategoryType.BOOKMARK, users)
        .stream()
        .collect(Collectors.toMap(Category::getOwner, Functions.identity()));
    // create bookmark and bookmark-post mapping
    List<Category> bookmarks = new ArrayList<>();
    List<CategoryPost> postMappings = new ArrayList<>();
    for (User user : users) {
      Category bookmark = userBookmarkMap.get(user);
      if (bookmark == null) {
        bookmark = categoryRepository.save(createBookmark(user));
      }
      bookmarks.add(bookmark);
      for (Post post : selectedPosts) {
        postMappings.add(createBookmarkPost(bookmark, post));
      }
    }
    categoryPostRepository.saveAll(postMappings);
    return bookmarks;
  }
}
