package com.theme.config;

import com.github.javafaker.Faker;
import com.theme.domain.Gender;
import com.theme.domain.User;
import com.theme.post.domain.Comment;
import com.theme.post.domain.Post;
import com.theme.post.domain.Prefer;
import com.theme.post.domain.Tag;
import com.theme.post.repository.CommentRepository;
import com.theme.post.repository.PostRepository;
import com.theme.post.repository.PreferRepository;
import com.theme.post.repository.TagRepository;
import com.theme.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataGenerator {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final PreferRepository preferRepository;
  private final TagRepository tagRepository;

  @PostConstruct
  public void init() {
    Faker faker = new Faker(Locale.ENGLISH);
    List<User> users = generateTestUsers(faker, 10);
    List<Post> posts = generateTestPosts(faker, users, 5);
    List<Comment> comments = generateTestComments(faker, posts, users, 3);
    List<Tag> tags = generateTestTags(faker, posts, 3);
    List<Prefer> prefers = generateTestPrefers(faker, posts, users, 5);
  }

  public List<User> generateTestUsers(Faker faker, int size) {
    if (userRepository.count() > 0) {
      return userRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<User> users = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      User user = User.builder()
          .userEmail(faker.internet().emailAddress())
          .birth(LocalDate.now().minusYears(i))
          .gender(i % 2 == 0 ? Gender.male : Gender.female)
          .profileImg(faker.internet().image())
          .introduce(faker.lorem().word())
          .build();
      users.add(user);
    }
    return userRepository.saveAll(users);
  }

  public List<Post> generateTestPosts(Faker faker, List<User> users, int size) {
    if (postRepository.count() > 0) {
      return postRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<Post> posts = new ArrayList<>();
    for (User user : users) {
      for (int j = 0; j < size; j++) {
        Post post = Post.builder()
            .title(faker.lorem().word())
            .content(faker.lorem().paragraph())
            .user(user)
            .build();
        posts.add(post);
      }
    }
    return postRepository.saveAll(posts);
  }

  public List<Comment> generateTestComments(Faker faker, List<Post> posts, List<User> users,
      int size) {
    if (commentRepository.count() > 0) {
      return commentRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<Comment> comments = new ArrayList<>();
    for (Post post : posts) {
      for (int j = 0; j < size; j++) {
        User author = users.get(RandomUtils.nextInt(users.size()));
        Comment comment = Comment.builder()
            .user(author)
            .post(post)
            .content(faker.lorem().sentence())
            .build();
        comments.add(comment);
      }
    }
    return commentRepository.saveAll(comments);
  }

  public List<Tag> generateTestTags(Faker faker, List<Post> posts, int size) {
    if (tagRepository.count() > 0) {
      return tagRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<Tag> tags = new ArrayList<>();
    for (Post post : posts) {
      for (int j = 0; j < size; j++) {
        Tag tag = Tag.builder()
            .tagName(faker.lorem().word())
            .post(post)
            .build();
        tags.add(tag);
      }
    }
    return tagRepository.saveAll(tags);
  }

  public List<Prefer> generateTestPrefers(Faker faker, List<Post> posts, List<User> users,
      int size) {
    if (preferRepository.count() > 0) {
      return preferRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<Prefer> prefers = new ArrayList<>();
    for (Post post : posts) {
      for (int j = 0; j < size; j++) {
        User user = users.get(RandomUtils.nextInt(users.size()));
        if (preferRepository.existsByUserAndPost(user, post)) {
          continue;
        }
        prefers.add(Prefer.builder()
            .post(post)
            .user(user)
            .build());
      }
    }
    return preferRepository.saveAll(prefers);
  }
}
