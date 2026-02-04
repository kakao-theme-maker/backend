package com.komentum.post.facade;

import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.service.CommentService;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Exit Plan : 150 Lines
@Service
@RequiredArgsConstructor
public class CommentManagementFacade {

  private final CommentService commentService;
  private final UserRetrieveService userRetrieveService;
  private final PostService postService;

  @Transactional
  public CommentResponse createCommentOnPost(Long postId, CommentCreateDto createDto,
      String authorId) {
    Post post = postService.getPostByPostId(postId);
    User author = userRetrieveService.findUserEntity(authorId);
    Comment savedComment = commentService.saveComment(post, author, createDto);
    return CommentResponse.from(savedComment);
  }
}
