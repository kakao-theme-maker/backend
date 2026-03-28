package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostSeeder {

  private final PostRepository postRepository;
  private final Faker faker;

  public Post createOne(User author, String previewImageName) {
    return postRepository.save(Post.builder()
        .title(faker.lorem().word())
        .content(faker.lorem().paragraph())
        .previewImageName(previewImageName)
        .user(author)
        .build());
  }

  public List<Post> seedPerUser(List<User> authors, int count, String previewImageName) {
    List<Post> posts = new ArrayList<>();
    for (User author : authors) {
      for (int i = 0; i < count; i++) {
        posts.add(createOne(author, previewImageName));
      }
    }
    return postRepository.saveAll(posts);
  }
}
