package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.DesignBoardRepositorySupport;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignBoardService {

  private final DesignBoardRepository designBoardRepository;
  private final DesignBoardRepositorySupport designBoardRepositorySupport;
  private final PostService postService;
  private final PostDtoMapper postDtoMapper;
  private final JPAQueryFactory queryFactory;

  @Transactional(readOnly = true)
  public List<DesignBoard> findAllByPostIds(List<Long> postIds) {
    QDesignBoard designBoard = QDesignBoard.designBoard;
    return queryFactory.selectFrom(designBoard)
        .where(designBoard.post.postId.in(postIds))
        .fetch();
  }

  @Transactional(readOnly = true)
  public DesignBoardQuery.Detail findDetailById(Long postId, User client) {
    return designBoardRepositorySupport.findDetailByPostId(postId, client);
  }

  @Transactional(readOnly = true)
  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable) {
    return designBoardRepositorySupport.findPreviewList(pageable);
  }

  @Transactional(readOnly = true)
  public DesignBoard findByPostId(long postId) {
    return designBoardRepository.findByPost_PostId(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(DesignBoard.class, postId));
  }

  @Transactional
  public DesignBoard save(Post post, DesignComponent designComponent) {
    return designBoardRepository.save(DesignBoard.builder()
        .post(post)
        .designComponent(designComponent)
        .build());
  }

  @Transactional
  public void deleteByPostId(Long postId) {
    designBoardRepository.deleteByPost_PostId(postId);
  }
}