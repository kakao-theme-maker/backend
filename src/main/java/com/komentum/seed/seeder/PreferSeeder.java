package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PreferRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PreferSeeder {

  private final PreferRepository preferRepository;
  private final PostRepository postRepository;
  private final Faker faker;

  private Prefer generateOne(Post post, User liker) {
    return Prefer.builder()
        .user(liker)
        .post(post)
        .build();
  }

  @Transactional
  public List<Prefer> seedPerPost(int preferPerPost, List<User> likerList) {
    if (likerList.size() < preferPerPost) {
      throw new IllegalArgumentException(
          "liker list size must be bigger than prefer count per post");
    }
    List<Post> posts = postRepository.findAll();
    List<Prefer> existingPrefers = preferRepository.fetchJoinByPostIn(posts);
    Map<User, Set<Post>> userPostPreferMap = existingPrefers.stream()
        .collect(Collectors.groupingBy(
            Prefer::getUser,
            Collectors.mapping(
                Prefer::getPost,
                Collectors.toSet()
            )));
    Map<Post, Set<Prefer>> existingPostPreferMap = existingPrefers.stream()
        .collect(Collectors.groupingBy(
            Prefer::getPost,
            Collectors.toSet()));
    List<Prefer> prefers = new ArrayList<>();
    for (Post post : posts) {
      Set<Prefer> existing = existingPostPreferMap.getOrDefault(post, Collections.emptySet());
      int created = existing.size();
      for (User liker : likerList) {
        Set<Post> alreadyPreferPosts = userPostPreferMap.getOrDefault(liker,
            Collections.emptySet());
        if (created >= preferPerPost) {
          break;
        } else if (alreadyPreferPosts.contains(post)) {
          continue;
        }
        created++;
        prefers.add(generateOne(post, liker));
      }
    }
    return preferRepository.saveAll(prefers);
  }
}
