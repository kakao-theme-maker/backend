package com.komentum.test;

import com.github.javafaker.Faker;
import com.komentum.config.PostTestDataGenerator;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CategoryPostDataGenerator {

  @Autowired
  private PostTestDataGenerator postTestDataGenerator;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private CategoryPostRepository categoryPostRepository;

  private List<Category> categories;
  private List<CategoryPost> categoryPosts;
  private List<Post> categoryRegisteredPosts;
  private List<Post> categoryUnregisteredPosts;

  public void generateAllData(int userCount, int postPerUser, int commentPerPost,
      int categoryCount) {
    postTestDataGenerator.generateData(userCount, postPerUser, commentPerPost);
    this.categories = generateCategoryData(categoryCount, postTestDataGenerator.users);
    this.categoryRegisteredPosts = postTestDataGenerator.posts.subList(0,
        postTestDataGenerator.posts.size() / 2);
    this.categoryUnregisteredPosts = postTestDataGenerator.posts.subList(
        postTestDataGenerator.posts.size() / 2, postTestDataGenerator.posts.size());
    this.categoryPosts = generateCategoryPostData(categories, categoryRegisteredPosts);
  }

  public void deleteAllData() {
    postTestDataGenerator.deleteData();
    categoryPostRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  public List<Category> generateCategoryData(int categoryCount, List<User> owners) {
    List<Category> res = new ArrayList<>();
    Faker faker = new Faker();
    for (int i = 0; i < categoryCount; i++) {
      res.add(categoryRepository.save(Category.builder()
          .name(faker.animal().name())
          .owner(owners.get(i % owners.size()))
          .build()));
    }
    return res;
  }

  public List<CategoryPost> generateCategoryPostData(List<Category> categories, List<Post> posts) {
    List<CategoryPost> res = new ArrayList<>();
    for (int i = 0; i < posts.size() / 2; i++) {
      res.add(categoryPostRepository.save(CategoryPost.builder()
          .category(categories.get(i % categories.size()))
          .post(posts.get(i))
          .build()));
    }
    return res;
  }

  public List<User> getUsers() {
    return postTestDataGenerator.users;
  }
}
