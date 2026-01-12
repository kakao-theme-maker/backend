package com.komentum.post.facade;

import com.komentum.post.domain.Post;
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

// TODO : 태그 기능 추가 시 태그 생성 / 조회 로직 추가하기
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

  /**
   * 게시글 ID를 기반으로 테마 게시글 상세 정보 반환
   * @param postId 게시글 ID
   * @return ThemeBoardDetailDto 테마 게시글 상세 정보
   * */
  @Transactional(readOnly = true)
  public ThemeBoardDetailDto findThemeBoardDetail(Long postId) {
    PostSummary postSummary = postRepositorySupport.findPostSummaryByPostId(postId);
    ThemeBoard themeBoard = themeBoardService.findByPostId(postId);
    String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
        postSummary.findPreviewImageName());
    return ThemeBoardDetailDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
        postSummary.getAuthor(), postSummary.getPrefers(), previewImageUrl);
  }

  /**
   * 페이지 기반 테마 게시글 목록 조회
   * @param pageable 페이지 기반 조회를 위한 페이지 정보 객체
   * @return ThemeBoardPreviewDto 목록
   * */
  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findThemeBoardPreviews(Pageable pageable) {
    List<PostSummary> postSummaries = postRepositorySupport.findPostSummaries(pageable);
    List<Long> postIds = postSummaries.stream().map(PostSummary::findPostId).toList();
    Map<Long, ThemeBoard> postThemeMap = themeBoardService.findAllByPostIds(postIds).stream()
        .collect(Collectors.toMap(ThemeBoard::findPostId, Function.identity()));
    return postSummaries.stream().map(postSummary -> {
          ThemeBoard themeBoard = postThemeMap.get(postSummary.findPostId());
          String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
              postSummary.findPreviewImageName());
          return ThemeBoardPreviewDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
              postSummary.getAuthor(),
              postSummary.getPrefers(), previewImageUrl);
        })
        .toList();
  }

  /**
   * 테마 게시글과 태그 정보 생성
   * @param createDto 게시글 생성 정보
   * @param previewImage 게시글 대표 이미지 정보
   * @param authorEmail 작성자 이메일
   * */
  @Transactional
  public ThemeBoardDetailDto createThemeBoard(
      ThemeBoardCreateDto createDto, MultipartFile previewImage, String authorEmail) {
    User author = userRetrieveService.findUserEntity(authorEmail);
    Post savedPost = boardManagementHelper.createPostAndPreviewImage(
        postDtoMapper.toPostCreateDto(createDto), author, previewImage);
    ThemeComponent themeComponent = themeRetrieveService.getThemeEntityById(
        createDto.getThemeComponentId());
    themeBoardService.save(savedPost, themeComponent);
    return findThemeBoardDetail(savedPost.getPostId());
  }

  /**
   * 테마 게시글과 태그 정보 수정
   * @param postId 테마 게시글 ID
   * @param updateDto 게시글 수정 정보
   * @param editorEmail 수정자 이메일
   * */
  @Transactional
  public ThemeBoardDetailDto updateThemeBoard(Long postId,
      ThemeBoardUpdateDto updateDto, String editorEmail) {
    User editor = userRetrieveService.findUserEntity(editorEmail);
    postService.updatePost(postId, editor,
        postDtoMapper.toPostUpdateDto(updateDto));
    return findThemeBoardDetail(postId);
  }

  /**
   * 테마 게시글 삭제
   * @param postId 테마 게시글 ID
   * */
  @Transactional
  public void deleteThemeBoard(Long postId) {
    themeBoardService.deleteByPostId(postId);
    postService.deletePost(postId);
  }
}
