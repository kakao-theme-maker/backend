package com.komentum.post.service;

import com.komentum.domain.User;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

  public List<Comment> getComments(Long postId, int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return commentRepository.findAllByPost_PostId(postId, pageable);
  }

  public Comment getCommentById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new NotFoundException("Comment not found"));
  }

  @Transactional
  public Comment saveComment(CommentCreateDto commentCreateDto) {
    Post post = postRepository.findById(commentCreateDto.getPostId())
        .orElseThrow(() -> new NotFoundException("Post not found"));
    User user = userRepository.findById(commentCreateDto.getUserEmail())
        .orElseThrow(() -> new NotFoundException("User not found"));
    Comment comment = Comment.createTransient(commentCreateDto, post, user);
    return commentRepository.save(comment);
  }

  @Transactional
  public Comment updateComment(Long commentId, CommentUpdateDto commentUpdateDto) {
    Comment comment = getCommentById(commentId);
    comment.update(commentUpdateDto);
    return commentRepository.save(comment);
  }

  @Transactional
  public void deleteComment(Long commentId) {
    Comment targetComment = getCommentById(commentId);
    commentRepository.deleteById(targetComment.getCommentId());
  }
}
