package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CategorySeeder {

  private final CategoryRepository categoryRepository;
  private final Faker faker;
  private final CategoryPostRepository categoryPostRepository;

  public Category createOneCategory(User user) {
    return Category.builder()
        .name(faker.animal().name())
        .owner(user)
        .build();
  }

  public CategoryPost createOneCategoryPost(Category category, Post post) {
    return CategoryPost.builder()
        .category(category)
        .post(post)
        .build();
  }

  @Transactional
  public List<Category> seedCategoryPerUser(int categoryPerUser, List<User> users) {
    List<Category> categories = new ArrayList<>();
    for (User user : users) {
      for (int i = 0; i < categoryPerUser; i++) {
        categories.add(createOneCategory(user));
      }
    }
    return categoryRepository.saveAll(categories);
  }

  @Transactional
  public void seedPostMappingsPerCategory(int categoryPostPerCategory, List<Category> categories,
      List<Post> posts) {
    List<CategoryPost> categoryPosts = new ArrayList<>();
    for (int i = 0; i < categories.size(); i++) {
      Category category = categories.get(i);
      for (int j = 0; j < categoryPostPerCategory; j++) {
        categoryPosts.add(createOneCategoryPost(category, posts.get(j % posts.size())));
      }
    }
    categoryPostRepository.saveAll(categoryPosts);
  }
}
