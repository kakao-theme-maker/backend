package com.komentum.test.data;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.repository.CommentLikeRepository;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PreferRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostTestDataGenerator {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private PreferRepository preferRepository;

  @Autowired
  private CommentLikeRepository commentLikeRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Getter
  public List<User> users;

  public List<Post> posts;

  public List<Comment> comments;

  public List<Prefer> prefers;

  public void generateData(int userCount, int postPerUser, int commentPerPost) {
    this.users = userDataGenerator.generateTestUsers(userCount);
    this.posts = generatePost(users, postPerUser);
    this.comments = generateComments(posts, commentPerPost);
  }

  public void generateDataWithPrefer(int userCount, int postPerUser, int commentPerPost,
      int maxPreferPerPost) {
    generateData(userCount, postPerUser, commentPerPost);
    this.prefers = generatePrefers(maxPreferPerPost);
  }

  public void deleteData() {
    commentLikeRepository.deleteAll();
    preferRepository.deleteAll();
    commentRepository.deleteAll();
    postRepository.deleteAll();
    userDataGenerator.deleteAllUsers();
  }

  public List<Post> generatePost(List<User> users, int postPerUser) {
    Faker faker = new Faker();
    List<Post> posts = new ArrayList<>();
    for (User user : users) {
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
    for (Post post : posts) {
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

  public List<Prefer> generatePrefers(int maxPreferPerPost) {
    List<Prefer> prefers = new ArrayList<>();
    for (int i = 0; i < posts.size(); i++) {
      Post post = posts.get(i);
      for (int j = 1; j <= maxPreferPerPost - i; j++) {
        prefers.add(Prefer.builder()
            .post(post)
            .user(users.get(j % users.size()))
            .build());
      }
    }
    return preferRepository.saveAll(prefers);
  }
}
