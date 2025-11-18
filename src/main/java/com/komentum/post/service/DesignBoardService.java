package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.QDesignComponentBoard;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.theme.component.domain.DesignComponent;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignBoardService {

  private final DesignBoardRepository designBoardRepository;
  private final JPAQueryFactory queryFactory;

  @Transactional(readOnly = true)
  public List<DesignBoard> findAllByPostIds(List<Long> postIds) {
    QDesignComponentBoard qDesignComponentBoard = QDesignComponentBoard.designComponentBoard;
    return queryFactory.selectFrom(qDesignComponentBoard)
        .where(qDesignComponentBoard.post.postId.in(postIds))
        .fetch();
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