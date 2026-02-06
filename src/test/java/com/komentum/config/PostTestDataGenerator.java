package com.komentum.config;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.UserDataGenerator;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostTestDataGenerator {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Getter
  public List<User> users;

  public List<Post> posts;

  public List<Comment> comments;

  public void generateData(int userCount, int postPerUser, int commentPerPost) {
    this.users = generateUsers(userCount);
    this.posts = generatePost(users, postPerUser);
    this.comments = generateComments(posts, commentPerPost);
  }

  public void deleteData() {
    commentRepository.deleteAll();
    postRepository.deleteAll();
    userDataGenerator.deleteAllUsers();
  }

  public List<User> generateUsers(int userCount) {
    return userDataGenerator.generateTestUsers(userCount);
  }

  public List<Post> generatePost(List<User> users, int postPerUser) {
    Faker faker = new Faker();
    List<Post> posts = new ArrayList<>();
    for (int i = 0; i < users.size(); i++) {
      User user = users.get(i);
      for (int j = 0; j < postPerUser; j++) {
        posts.add(Post.builder()
            .title(faker.lorem().sentence())
            .content(faker.lorem().paragraph())
            .user(user)
            .previewImageName(UUID.randomUUID().toString())
            .build());
      }
    }
    return postRepository.saveAll(posts);
  }

  public List<Comment> generateComments(List<Post> posts, int commentPerPost) {
    Faker faker = new Faker();
    List<Comment> comments = new ArrayList<>();
    for (int i = 0; i < posts.size(); i++) {
      Post post = posts.get(i);
      for (int j = 0; j < commentPerPost; j++) {
        comments.add(Comment.builder()
            .content(faker.lorem().sentence())
            .post(post)
            .user(post.getUser())
            .build());
      }
    }
    return commentRepository.saveAll(comments);
  }
}
