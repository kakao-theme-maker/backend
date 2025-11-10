package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeBoardService {

  private final ThemeBoardRepository themeBoardRepository;

  @Transactional(readOnly = true)
  public List<ThemeBoard> findAll(Pageable pageable) {
    return themeBoardRepository.findAll(pageable).getContent();
  }

  @Transactional(readOnly = true)
  public List<ThemeBoard> findAllByPostIds(List<Long> postIds) {
    return themeBoardRepository.findAllByPostIds(postIds);
  }

  @Transactional(readOnly = true)
  public ThemeBoard findByPostId(Long postId) {
    return themeBoardRepository.findByPost_PostId(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(ThemeBoard.class, postId));
  }

  @Transactional
  public ThemeBoard save(Post post, ThemeComponent themeComponent) {
    return themeBoardRepository.save(ThemeBoard.builder()
        .post(post)
        .themeComponent(themeComponent)
        .build());
  }

  @Transactional
  public void deleteByPostId(Long postId) {
    themeBoardRepository.deleteByPost_PostId(postId);
  }
}
