package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CommentSeeder {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final Faker faker;

  private Comment generateOne(Post post, User author) {
    return Comment.builder()
        .content(faker.lorem().sentence())
        .post(post)
        .user(author)
        .build();
  }

  @Transactional
  public List<Comment> seedPerPost(int size, List<User> authors) {
    if (authors.isEmpty()) {
      throw new IllegalArgumentException("CommentSeeder : author size = 0");
    }
    List<Post> posts = postRepository.findAll();
    List<Comment> existing = commentRepository.findByPostIn(posts);
    if (existing.size() >= size * posts.size()) {
      return existing;
    }
    List<Comment> comments = new ArrayList<>();
    for (int i = 0; i < posts.size(); i++) {
      Post post = posts.get(i);
      for (int j = 0; j < size; j++) {
        comments.add(generateOne(post, authors.get((i + j) % authors.size())));
      }
    }
    return commentRepository.saveAll(comments);
  }
}
