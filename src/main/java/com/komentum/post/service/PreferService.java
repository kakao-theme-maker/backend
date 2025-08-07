package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.dto.PreferDto.PreferCreateDto;
import com.komentum.post.dto.PreferDto.PreferDeleteDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PreferRepository;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferService {

  private final PreferRepository preferRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public Long getPreferByPost(Long postId) {
    return preferRepository.countPreferByPost_PostId(postId);
  }

  @Transactional
  public void savePrefer(Long postId, PreferCreateDto createDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));
    User user = userRepository.findById(createDto.getUserEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    if (preferRepository.existsByUserAndPost(user, post)) {
      throw new RuntimeException("Prefer already exists");
    }
    preferRepository.save(Prefer.createTransient(post, user));
  }

  @Transactional
  public void deletePrefer(Long postId, PreferDeleteDto deleteDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));
    User user = userRepository.findById(deleteDto.getUserEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    if (!preferRepository.existsByUserAndPost(user, post)) {
      throw new RuntimeException("User Not Prefered yet");
    }
    preferRepository.deleteByUserAndPost(user, post);
  }
}
