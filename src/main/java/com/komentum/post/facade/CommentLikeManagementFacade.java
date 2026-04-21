package com.komentum.post.facade;

import com.komentum.post.service.CommentLikeService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentLikeManagementFacade {

  private final CommentLikeService commentLikeService;
  private final UserEntityFinder userEntityFinder;

  public void like(Long commentId, String clientId) {
    User targetUser = userEntityFinder.findUserEntity(clientId);
    commentLikeService.like(commentId, targetUser.getUserId());
  }

  public void unlike(Long commentId, String clientId) {
    User targetUser = userEntityFinder.findUserEntity(clientId);
    commentLikeService.unlike(commentId, targetUser.getUserId());
  }
}
