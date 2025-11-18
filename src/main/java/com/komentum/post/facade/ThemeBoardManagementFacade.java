package com.komentum.post.facade;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.repository.PostRepositorySupport;
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
import org.springframework.web.multipart.MultipartFile;

// Exit Plan: 150 lines
@Service
@RequiredArgsConstructor
public class ThemeBoardManagementFacade {

  private final PostService postService;
  private final PostRepositorySupport postRepositorySupport;
  private final TagService tagService;
  private final UserRetrieveService userRetrieveService;
  private final ThemeBoardService themeBoardService;
  private final ThemeRetrieveService themeRetrieveService;
  private final BoardManagementHelper boardManagementHelper;

  // mapper
  private final PostDtoMapper postDtoMapper;

  @Transactional(readOnly = true)
  public ThemeBoardDetailDto findThemeBoardDetail(Long boardId) {
    PostSummary postSummary = postRepositorySupport.findPostSummaryByPostId(boardId);
    List<Tag> tags = tagService.getTagsByPostId(boardId);
    ThemeBoard themeBoard = themeBoardService.findByPostId(boardId);
    String profileImageUrl = boardManagementHelper.findProfileImageUrl(
        postSummary.findProfileImageName());
    return ThemeBoardDetailDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
        postSummary.getAuthor(), tags, postSummary.getPrefers(), profileImageUrl);
  }

  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findThemeBoardPreviews(Pageable pageable) {
    List<PostSummary> postSummaries = postRepositorySupport.findPostSummaries(pageable);
    List<Long> postIds = postSummaries.stream().map(PostSummary::findPostId).toList();
    Map<Long, ThemeBoard> postThemeMap = themeBoardService.findAllByPostIds(postIds).stream()
        .collect(Collectors.toMap(ThemeBoard::findPostId, Function.identity()));
    return postSummaries.stream().map(postSummary -> {
          ThemeBoard themeBoard = postThemeMap.get(postSummary.findPostId());
          String profileImageUrl = boardManagementHelper.findProfileImageUrl(
              postSummary.findProfileImageName());
          return ThemeBoardPreviewDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
              postSummary.getAuthor(),
              postSummary.getPrefers(), profileImageUrl);
        })
        .toList();
  }

  @Transactional
  public ThemeBoardDetailDto createThemeBoardWithTags(
      ThemeBoardCreateDto createDto, MultipartFile profileImage) {
    User author = userRetrieveService.findUserEntity(createDto.getUserEmail());
    Post savedPost = boardManagementHelper.createPostAndProfileImage(
        postDtoMapper.toPostCreateDto(createDto), author, profileImage);
    tagService.createTags(savedPost, createDto.getPostTags());
    ThemeComponent themeComponent = themeRetrieveService.getThemeEntityById(
        createDto.getThemeComponentId());
    themeBoardService.save(savedPost, themeComponent);
    return findThemeBoardDetail(savedPost.getPostId());
  }

  @Transactional
  public ThemeBoardDetailDto updateThemeBoardWithTags(Long boardId,
      ThemeBoardUpdateDto updateDto) {
    User editor = userRetrieveService.findUserEntity(updateDto.getUserEmail());
    Post updatedPost = postService.updatePost(boardId, editor,
        postDtoMapper.toPostUpdateDto(updateDto));
    tagService.synchronizeTags(updatedPost,
        updateDto.getPostTags());
    return findThemeBoardDetail(boardId);
  }

  @Transactional
  public void deleteThemeBoard(Long boardId) {
    themeBoardService.deleteByPostId(boardId);
    postService.deletePost(boardId);
  }
}
