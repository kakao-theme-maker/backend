package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.TagRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TagSeeder {

  private final TagRepository tagRepository;
  private final PostRepository postRepository;
  private final Faker faker;

  private Tag generateOne(Post targetPost) {
    return Tag.builder()
        .post(targetPost)
        .tagName(faker.lorem().word())
        .build();
  }

  @Transactional
  public List<Tag> seedPerPost(int tagPerPost) {
    List<Post> posts = postRepository.findAll();
    List<Tag> tags = new ArrayList<>();
    for (Post post : posts) {
      for (int i = 0; i < tagPerPost; i++) {
        tags.add(generateOne(post));
      }
    }
    return tagRepository.saveAll(tags);
  }
}
