package com.komentum.post.service;

import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.post.mapper.ThemeBoardMapperSupport;
import com.komentum.post.repository.PostRepositorySupport;
import com.komentum.post.repository.ThemeBoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeBoardQueryService {

  private final PostRepositorySupport postRepositorySupport;
  private final BoardManagementHelper boardManagementHelper;
  private final ThemeBoardMapperSupport themeBoardMapperSupport;
  private final ThemeBoardRepository themeBoardRepository;

  @Transactional(readOnly = true)
  public ThemeBoardDetailDto findThemeBoardDetail(Long postId) {
    ThemeBoard themeBoard = themeBoardRepository.findByPost_PostId(postId)
        .orElseThrow(() -> new EntityNotFoundException("theme board doesn't exists"));
    return themeBoardMapperSupport.toThemeBoardDetailDto(
        postRepositorySupport.findPostSummaryByPostId(postId),
        themeBoard,
        boardManagementHelper
    );
  }
}
