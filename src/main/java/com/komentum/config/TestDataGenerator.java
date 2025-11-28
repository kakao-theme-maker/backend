package com.komentum.config;

import com.github.javafaker.Faker;
import com.komentum.global.security.UserRole;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PreferRepository;
import com.komentum.post.repository.TagRepository;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TestDataGenerator {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final PreferRepository preferRepository;
  private final TagRepository tagRepository;
  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeBoardRepository themeBoardRepository;

  @PostConstruct
  @Transactional
  public void init() {
    Faker faker = new Faker(Locale.ENGLISH);
    List<User> users = generateTestUsers(faker, 10);
    List<Post> posts = generateTestPosts(faker, users, 5);
    List<ThemeComponent> themeComponents = generateTestThemeComponents(faker, users, posts.size());
    List<ThemeBoard> themeBoards = generateTestThemeBoards(faker, posts, themeComponents);
    List<Comment> comments = generateTestComments(faker, posts, users, 3);
    List<Tag> tags = generateTestTags(faker, posts, 3);
    List<Prefer> prefers = generateTestPrefers(faker, posts, users, 5);
  }

  public List<User> generateTestUsers(Faker faker, int size) {
    if (userRepository.count() > 0) {
      return userRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    Set<String> userEmails = new HashSet<>();
    List<User> users = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      User user = User.builder()
          .userEmail(faker.internet().emailAddress())
          .role(UserRole.USER)
          .birth(LocalDate.now().minusYears(i))
          .gender(i % 2 == 0 ? Gender.male : Gender.female)
          .profileImg(faker.internet().image())
          .introduce(faker.lorem().word())
          .build();
      if (!userEmails.contains(user.getUserEmail())) {
        users.add(user);
        userEmails.add(user.getUserEmail());
      }
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
        User author = users.get(ThreadLocalRandom.current().nextInt(users.size()));
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
        User author = users.get(ThreadLocalRandom.current().nextInt(users.size()));
        if (preferRepository.existsByUserAndPost(author, post)) {
          continue;
        }
        prefers.add(Prefer.builder()
            .post(post)
            .user(author)
            .build());
      }
    }
    return preferRepository.saveAll(prefers);
  }

  public List<ThemeComponent> generateTestThemeComponents(Faker faker, List<User> users, int size) {
    if (themeComponentRepository.count() > 0) {
      return themeComponentRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<ThemeComponent> themeComponents = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      themeComponents.add(themeComponentRepository.save(ThemeComponent.builder()
          .themeName(faker.lorem().word())
          .userEmail(users.get(0).getUserEmail())
          .versionName(faker.lorem().word())
          .versionNumber("0")
          .isDone(true)
          .build()));
    }
    return themeComponentRepository.saveAll(themeComponents);
  }

  public List<ThemeBoard> generateTestThemeBoards(Faker faker, List<Post> posts,
      List<ThemeComponent> themeComponents) {
    if (themeBoardRepository.count() > 0) {
      return themeBoardRepository.findAll(PageRequest.of(0, 10)).getContent();
    }
    List<ThemeBoard> themeBoards = new ArrayList<>();
    int size = Math.max(posts.size(), themeComponents.size());
    for (int i = 0; i < size; i++) {
      themeBoards.add(themeBoardRepository.save(ThemeBoard.builder()
          .post(posts.get(i))
          .themeComponent(themeComponents.get(i))
          .build()));
    }
    return themeBoards;
  }
}
