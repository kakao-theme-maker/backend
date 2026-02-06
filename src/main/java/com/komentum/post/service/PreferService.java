package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.domain.policy.PreferPolicy;
import com.komentum.post.repository.PreferRepository;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PreferService {

  private final PreferRepository preferRepository;
  private final PreferPolicy preferPolicy;

  public Long getPreferByPost(Long postId) {
    return preferRepository.countPreferByPost_PostId(postId);
  }

  public Prefer findPreferByUserAndPost(User liker, Post targetPost) {
    return preferRepository.findByUserAndPost(liker, targetPost)
        .orElseThrow(
            () -> new EntityNotFoundException("failed to find prefer : invalid user or post"));
  }

  @Transactional
  public void savePrefer(User liker, Post targetPost) {
    if (preferRepository.existsByUserAndPost(liker, targetPost)) {
      throw new RuntimeException("Prefer already exists");
    }
    preferRepository.save(Prefer.createTransient(targetPost, liker));
  }

  @Transactional
  public void deletePrefer(User liker, Post targetPost) {
    Prefer prefer = findPreferByUserAndPost(liker, targetPost);
    if (!preferPolicy.canDelete(prefer.getUser())) {
      throw new AccessDeniedException("failed to delete prefer : invalid user or role");
    }
    preferRepository.deleteByUserAndPost(liker, targetPost);
  }
}
