package com.komentum.post.facade;

import com.komentum.post.domain.Post;
import com.komentum.post.service.PostService;
import com.komentum.post.service.PreferService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Exit Plan : 150 Lines
@Service
@RequiredArgsConstructor
public class PreferManagementFacade {

  private final PostService postService;
  private final UserRetrieveService userRetrieveService;
  private final PreferService preferService;

  public void addPreferToPost(Long postId, String clientId) {
    User targetUser = userRetrieveService.findUserEntity(clientId);
    Post targetPost = postService.getPostByPostId(postId);
    preferService.savePrefer(targetUser, targetPost);
  }

  public void deletePreferFromPost(Long postId, String clientId) {
    User targetUser = userRetrieveService.findUserEntity(clientId);
    Post targetPost = postService.getPostByPostId(postId);
    preferService.deletePrefer(targetUser, targetPost);
  }
}
