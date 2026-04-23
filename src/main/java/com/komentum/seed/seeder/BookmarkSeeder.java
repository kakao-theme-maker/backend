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

  public static record BookmarkSeedResult(List<Category> bookmarks,
                                          List<CategoryPost> bookmarkMappings) {

  }

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
  public BookmarkSeedResult bookmarkByRatio(List<User> users, List<Post> posts, double ratio) {
    if (ratio < 0 || ratio > 1) {
      throw new IllegalArgumentException("BookmarkSeeder : ratio must be between 0 and 1");
    }
    // ratio를 기반으로 랜덤 순서의 post 목록 생성
    List<Post> shuffledPosts = new ArrayList<>(posts);
    Collections.shuffle(shuffledPosts);
    List<Post> selectedPosts = shuffledPosts.subList(0, (int) (posts.size() * ratio));
    // DB에서 사용자 - 북마크 카테고리 맵 조회
    Map<User, Category> userBookmarkMap = categoryRepository
        .fetchJoinAllByCategoryTypeAndOwnerIn(CategoryType.BOOKMARK, users)
        .stream()
        .collect(Collectors.toMap(Category::getOwner, Functions.identity()));
    // 사용자별 북마크 일괄 생성
    List<Category> newBookmarks = new ArrayList<>();
    List<Category> allBookmarks = new ArrayList<>();
    for (User user : users) {
      Category bookmark = userBookmarkMap.get(user);
      if (bookmark == null) {
        newBookmarks.add(createBookmark(user));
      } else {
        allBookmarks.add(bookmark);
      }
    }
    allBookmarks.addAll(categoryRepository.saveAll(newBookmarks));
    // 게시글 - 북마크 매핑 일괄 생성
    List<CategoryPost> postMappings = new ArrayList<>();
    for (Category bookmark : allBookmarks) {
      for (Post post : selectedPosts) {
        postMappings.add(createBookmarkPost(bookmark, post));
      }
    }
    List<CategoryPost> bookmarkMappings = categoryPostRepository.saveAll(postMappings);
    return new BookmarkSeedResult(allBookmarks, bookmarkMappings);
  }
}
