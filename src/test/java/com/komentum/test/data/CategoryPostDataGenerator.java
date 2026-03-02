package com.komentum.test.data;

import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

  public void generateCategoriesAndPosts(int userCount, int postPerUser, int commentPerPost,
      int categoryPerUser, int postPerCategory) {
    if (postPerCategory > userCount * postPerUser) {
      throw new RuntimeException("post count per category is bigger than total post count");
    }
    postTestDataGenerator.generateData(userCount, postPerUser, commentPerPost);
    this.categories = generateCategoryData(categoryPerUser, postTestDataGenerator.users);
    this.categoryPosts = generateCategoryPostData(postPerCategory, categories, getPosts());
    this.categoryRegisteredPosts = postTestDataGenerator.posts;
  }

  public void deleteAllData() {
    postTestDataGenerator.deleteData();
    categoryPostRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  public void generateCategoryUnregisteredPosts(int postPerUser) {
    this.categoryUnregisteredPosts = postTestDataGenerator.generatePost(getUsers(), postPerUser);
  }

  public List<Category> generateCategoryData(int categoryPerUser, List<User> owners) {
    List<Category> res = new ArrayList<>();
    for (User owner : owners) {
      for (int i = 0; i < categoryPerUser; i++) {
        res.add(Category.builder()
            .name(UUID.randomUUID().toString())
            .owner(owner)
            .build());
      }
    }
    return categoryRepository.saveAll(res);
  }

  public List<CategoryPost> generateCategoryPostData(int postPerCategory, List<Category> categories,
      List<Post> posts) {
    List<CategoryPost> res = new ArrayList<>();
    for (Category category : categories) {
      for (int i = 0; i < postPerCategory; i++) {
        res.add(CategoryPost.builder()
            .post(posts.get(i % posts.size()))
            .category(category)
            .build());
      }
    }
    return categoryPostRepository.saveAll(res);
  }

  public List<User> getUsers() {
    return postTestDataGenerator.users;
  }

  public List<Post> getPosts() {
    return postTestDataGenerator.posts;
  }
}
