package com.theme.post.service;

import com.theme.domain.User;
import com.theme.post.domain.Post;
import com.theme.post.domain.Prefer;
import com.theme.post.dto.PreferDto.PreferCreateDto;
import com.theme.post.dto.PreferDto.PreferDeleteDto;
import com.theme.post.repository.PostRepository;
import com.theme.post.repository.PreferRepository;
import com.theme.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
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
        .orElseThrow(() -> new NotFoundException("Post not found"));
    User user = userRepository.findById(createDto.getUserEmail())
        .orElseThrow(() -> new NotFoundException("User not found"));
    if (preferRepository.existsByUserAndPost(user, post)) {
      throw new RuntimeException("Prefer already exists");
    }
    preferRepository.save(Prefer.createTransient(post, user));
  }

  @Transactional
  public void deletePrefer(Long postId, PreferDeleteDto deleteDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
    User user = userRepository.findById(deleteDto.getUserEmail())
        .orElseThrow(() -> new NotFoundException("User not found"));
    if (!preferRepository.existsByUserAndPost(user, post)) {
      throw new RuntimeException("User Not Prefered yet");
    }
    preferRepository.deleteByUserAndPost(user, post);
  }
}
