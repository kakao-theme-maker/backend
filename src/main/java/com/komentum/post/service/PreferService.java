package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.repository.PreferRepository;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferService {

  private final PreferRepository preferRepository;

  public Long getPreferByPost(Long postId) {
    return preferRepository.countPreferByPost_PostId(postId);
  }

  @Transactional
  public void savePrefer(User targetUser, Post targetPost) {
    if (preferRepository.existsByUserAndPost(targetUser, targetPost)) {
      throw new RuntimeException("Prefer already exists");
    }
    preferRepository.save(Prefer.createTransient(targetPost, targetUser));
  }

  @Transactional
  public void deletePrefer(User targetUser, Post targetPost) {
    if (!preferRepository.existsByUserAndPost(targetUser, targetPost)) {
      throw new RuntimeException("User Not Prefered yet");
    }
    preferRepository.deleteByUserAndPost(targetUser, targetPost);
  }
}
