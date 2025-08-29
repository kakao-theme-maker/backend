package com.komentum.config;

import com.github.javafaker.Faker;
import com.komentum.global.security.UserRole;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  public List<User> users;

  public List<Post> posts;

  public List<Comment> comments;

  public void generateData(int userCount, int postPerUser, int commentPerPost) {
    Faker faker = new Faker();
    this.users = generateUser(userCount);
    this.posts = generatePost(users, postPerUser);
    this.comments = generateComments(posts, commentPerPost);
  }

  public void deleteData() {
    userRepository.deleteAll();
    postRepository.deleteAll();
    commentRepository.deleteAll();
  }

  public List<User> generateUser(int userCount) {
    Faker faker = new Faker();
    Set<String> userEmails = new HashSet<>();
    List<User> users = new ArrayList<>();
    for (int i = 0; i < userCount; i++) {
      User user = User.builder()
          .userEmail(String.format("test%s@test.com", i))
          .role(UserRole.USER)
          .birth(LocalDate.now())
          .introduce(faker.lorem().sentence())
          .gender(Gender.male)
          .profileImg(faker.internet().image())
          .build();
      if (!userEmails.contains(user.getUserEmail())) {
        userEmails.add(user.getUserEmail());
        users.add(userRepository.save(user));
      }
    }
    return users;
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
