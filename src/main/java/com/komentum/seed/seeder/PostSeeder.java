package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostSeeder {

  private final PostRepository postRepository;
  private final Faker faker;

  private Post generateOne(User author) {
    return Post.builder()
        .title(faker.lorem().word())
        .content(faker.lorem().paragraph())
        .previewImageName("test-preview-image-name")
        .user(author)
        .build();
  }

  @Transactional
  public List<Post> seedPerUser(int size, List<User> authors) {
    int expected = size * authors.size();
    List<Post> existing = postRepository.findByUserIn(authors);
    if (existing.size() >= expected) {
      return existing;
    }
    List<Post> posts = new ArrayList<>();
    for (User author : authors) {
      for (int i = 0; i < size; i++) {
        posts.add(generateOne(author));
      }
    }
    return postRepository.saveAll(posts);
  }
}
