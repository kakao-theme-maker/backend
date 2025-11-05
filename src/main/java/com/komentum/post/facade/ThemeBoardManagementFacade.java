package com.komentum.post.facade;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.PostDto.ThemeBoardDetailDto;
import com.komentum.post.dto.PostDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.service.PostService;
import com.komentum.post.service.TagService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserRetrieveService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Exit Plan : 150 lines
@Service
@RequiredArgsConstructor
public class ThemeBoardManagementFacade {

  private final PostService postService;
  private final TagService tagService;
  private final UserRetrieveService userRetrieveService;

  @Transactional(readOnly = true)
  public ThemeBoardDetailDto findThemeBoardDetail(Long postId) {
    PostSummary postSummary = postService.getPostSummaryByPostId(postId);
    List<Tag> tags = tagService.getTagsByPostId(postId);
    return ThemeBoardDetailDto.from(postSummary, tags);
  }

  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findThemeBoardPreviews(Pageable pageable) {
    Map<Long, PostSummary> postSummaryMap = postService.getPostSummaries(pageable)
        .stream()
        .collect(Collectors.toMap(p -> p.getPost().getPostId(), Function.identity()));
    return postSummaryMap.values()
        .stream()
        .map(postSummary -> ThemeBoardPreviewDto.from(postSummary.getPost(),
            postSummary.getPrefers()))
        .toList();
  }

  @Transactional
  public ThemeBoardDetailDto createThemeBoardWithTags(PostCreateDto postCreateDto) {
    User author = userRetrieveService.findUserEntity(postCreateDto.getUserEmail());
    Post savedPost = postService.createPost(postCreateDto, author);
    tagService.createTags(savedPost, postCreateDto.getTags());
    return ThemeBoardDetailDto.fromNewBoard(savedPost);
  }

  @Transactional
  public ThemeBoardDetailDto updateThemeBoardWithTags(Long boardId, PostUpdateDto updateDto) {
    User editor = userRetrieveService.findUserEntity(updateDto.getUserEmail());
    PostSummary postSummary = postService.getPostSummaryByPostId(boardId);
    if (!postSummary.getPost().getUser().equals(editor)) {
      throw new IllegalArgumentException("You are not allowed to change theme board");
    }
    List<Tag> updatedTags = tagService.synchronizeTags(postSummary.getPost(),
        updateDto.getPostTags());
    return ThemeBoardDetailDto.from(postSummary, updatedTags);
  }
}
