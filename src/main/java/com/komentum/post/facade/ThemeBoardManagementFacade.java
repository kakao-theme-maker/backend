package com.komentum.post.facade;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.service.PostService;
import com.komentum.post.service.TagService;
import com.komentum.post.service.ThemeBoardService;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.service.ThemeRetrieveService;
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

// Exit Plan: 150 lines
@Service
@RequiredArgsConstructor
public class ThemeBoardManagementFacade {

  private final PostService postService;
  private final TagService tagService;
  private final UserRetrieveService userRetrieveService;
  private final ThemeBoardService themeBoardService;
  private final ThemeRetrieveService themeRetrieveService;

  @Transactional(readOnly = true)
  public ThemeBoardDetailDto findThemeBoardDetail(Long postId) {
    PostSummary postSummary = postService.getPostSummaryByPostId(postId);
    List<Tag> tags = tagService.getTagsByPostId(postId);
    return ThemeBoardDetailDto.from(postSummary, tags);
  }

  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findThemeBoardPreviews(Pageable pageable) {
    List<PostSummary> postSummaries = postService.getPostSummaries(pageable);
    Map<Long, PostSummary> postSummaryMap = postSummaries.stream()
        .collect(Collectors.toMap(PostSummary::findPostId, Function.identity()));
    List<Long> postIds = postSummaries.stream().map(PostSummary::findPostId).toList();
    Map<Long, ThemeBoard> postThemeMap = themeBoardService.findAllByPostIds(postIds).stream()
        .collect(Collectors.toMap(ThemeBoard::findPostId, Function.identity()));
    return postSummaryMap.values()
        .stream()
        .map(postSummary -> {
          ThemeBoard themeBoard = postThemeMap.get(postSummary.getPost().getPostId());
          return ThemeBoardPreviewDto.from(themeBoard.getPost(), themeBoard.getThemeComponent(),
              postSummary.getPrefers());
        })
        .toList();
  }

  @Transactional
  public ThemeBoardDetailDto createThemeBoardWithTags(
      ThemeBoardCreateDto themeBoardCreateDto) {
    User author = userRetrieveService.findUserEntity(themeBoardCreateDto.getUserEmail());
    Post savedPost = postService.createPost(themeBoardCreateDto.toPostCreateDto(), author);
    List<Tag> tags = tagService.createTags(savedPost, themeBoardCreateDto.getPostTags());
    ThemeComponent themeComponent = themeRetrieveService.getThemeEntityBiId(
        themeBoardCreateDto.getThemeComponentId());
    themeBoardService.save(savedPost, themeComponent);
    return ThemeBoardDetailDto.from(new PostSummary(savedPost, 0L), tags);
  }

  @Transactional
  public ThemeBoardDetailDto updateThemeBoardWithTags(Long boardId,
      ThemeBoardUpdateDto updateDto) {
    User editor = userRetrieveService.findUserEntity(updateDto.getUserEmail());
    PostSummary postSummary = postService.getPostSummaryByPostId(boardId);
    if (!postSummary.getPost().getUser().equals(editor)) {
      throw new IllegalArgumentException("You are not allowed to change theme board");
    }
    Post post = postSummary.getPost();
    post.update(updateDto);
    List<Tag> updatedTags = tagService.synchronizeTags(postSummary.getPost(),
        updateDto.getPostTags());
    return ThemeBoardDetailDto.from(postSummary, updatedTags);
  }

  @Transactional
  public void deleteThemeBoard(Long boardId) {
    themeBoardService.deleteByPostId(boardId);
    postService.deletePost(boardId);
  }
}
