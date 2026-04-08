package com.komentum.post.facade;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.FileUtils;
import com.komentum.post.consts.ThemeBoardConsts;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.dto.query.ThemeBoardQuery;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.mapper.ThemeBoardMapperSupport;
import com.komentum.post.service.PostService;
import com.komentum.post.service.ThemeBoardService;
import com.komentum.post.service.transaction.ThemeBoardTransactionService;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.service.ThemeImageService;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
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
  private final UserEntityFinder userEntityFinder;
  private final ThemeBoardService themeBoardService;
  private final ThemeRetrieveService themeRetrieveService;
  private final BoardManagementHelper boardManagementHelper;
  private final ThemeBoardMapperSupport themeBoardMapperSupport;
  private final PostDtoMapper postDtoMapper;
  private final ThemeImageService themeImageService;
  private final FileManager fileManager;
  private final FileUtils fileUtils;
  private final ThemeBoardTransactionService themeBoardTransactionService;

  private String uploadOrReusePreviewImage(MultipartFile previewImage,
      ThemeComponent themeComponent) {
    // previewImage가 유효하지 않으면, ThemeComponent의 이미지 사용
    if (previewImage == null || previewImage.isEmpty()) {
      ThemeImage themeImage = themeImageService.findByThemeComponentAndComponentTypeName(
          themeComponent, ThemeBoardConsts.DEFAULT_COMPONENT_TYPE_NAME);
      DesignComponent designComponent = themeImage.getDesignComponent();
      String fileName = fileManager.convertUrlToFileName(designComponent.getImageUrl());
      byte[] previewImageBytes = fileManager.downloadFile(fileName);
      return boardManagementHelper
          .savePreviewImageIfPresent(Post.class, fileName, previewImageBytes);
    }
    // previewImage가 유효하면 previewImage 사용
    return boardManagementHelper.savePreviewImageIfPresent(Post.class, previewImage);
  }

  /**
   * 게시글 ID를 기반으로 테마 게시글 상세 정보 반환
   *
   * @param postId 게시글 ID
   * @return ThemeBoardDetailDto 테마 게시글 상세 정보
   */
  @Transactional
  public ThemeBoardDetailDto findThemeBoardDetail(Long postId, String userIdentifier) {
    return themeBoardTransactionService.findThemeBoardDetail(postId, userIdentifier);
  }

  /**
   * 페이지 기반 테마 게시글 목록 조회 기본값으로 날짜순으로 정렬
   *
   * @param pageable 페이지 기반 조회를 위한 페이지 정보 객체
   * @return ThemeBoardPreviewDto 목록
   */
  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findThemeBoardPreviews(Pageable pageable) {
    List<ThemeBoardQuery.Preview> themeBoardQueryPreviewList = themeBoardService.findThemeBoardQueryPreview(
        pageable);
    return themeBoardMapperSupport.toThemeBoardPreviewDtoList(themeBoardQueryPreviewList,
        boardManagementHelper);
  }

  /**
   * 좋아요 순으로 내림차순 정렬한 테마 게시글 목록 반환
   *
   * @param pageable 페이지 정보
   * @return ThemeBoardPreviewDto 목록
   */
  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findPopularThemeBoardPreviews(Pageable pageable) {
    List<ThemeBoardQuery.Preview> themeBoardQueryPreviewList = themeBoardService.findThemeBoardQueryPreviewOrderByPrefers(
        pageable, true);
    return themeBoardMapperSupport.toThemeBoardPreviewDtoList(themeBoardQueryPreviewList,
        boardManagementHelper);
  }

  /**
   * 임시로 사용할 추천 테마 게시글 목록 반환 기능
   * <p>현재 현재 추천 모델이 미개발된 상황이므로, 우선 테마 목록을 좋아요 순으로 오름차순 정렬한 데이터 사용</p>
   *
   * @param pageable 페이지 정보
   * @return ThemeBoardPreviewDto 목록
   */
  @Transactional(readOnly = true)
  public List<ThemeBoardPreviewDto> findRecommendedThemeBoardPreviews(Pageable pageable) {
    List<ThemeBoardQuery.Preview> themeBoardQueryPreviewList = themeBoardService.findThemeBoardQueryPreviewOrderByPrefers(
        pageable, false);
    return themeBoardMapperSupport.toThemeBoardPreviewDtoList(themeBoardQueryPreviewList,
        boardManagementHelper);
  }

  /**
   * 테마 게시글 생성
   *
   * @param createDto    게시글 생성 정보
   * @param previewImage 게시글 대표 이미지 정보
   * @param authorId     작성자 식별자
   */
  public ThemeBoardDetailDto createThemeBoard(
      ThemeBoardCreateDto createDto, MultipartFile previewImage, String authorId) {
    // 이미지 저장
    ThemeComponent themeComponent = themeRetrieveService.getThemeEntityById(
        createDto.getThemeComponentId());
    String previewImageName = uploadOrReusePreviewImage(previewImage, themeComponent);
    // DB 처리 + 커밋
    User author = userEntityFinder.findUserEntity(authorId);
    try {
      Post savedPost = themeBoardTransactionService.saveThemeBoardAndReturnPost(
          createDto,
          themeComponent,
          author,
          previewImageName);
      return themeBoardTransactionService.findThemeBoardDetail(savedPost.getPostId(), authorId);
    } catch (Exception e) {
      fileUtils.deleteFileSilently(previewImageName, "테마 게시글 생성 실패로 인한 저장된 파일 롤백");
      throw new RuntimeException("테마 게시글 생성 실패", e);
    }
  }

  /**
   * 테마 게시글 수정
   *
   * @param postId    테마 게시글 ID
   * @param updateDto 게시글 수정 정보
   */
  public ThemeBoardDetailDto updateThemeBoard(Long postId,
      ThemeBoardUpdateDto updateDto, MultipartFile previewImage, String userIdentifier) {
    // 파일 작업 처리
    String newImageName = boardManagementHelper.savePreviewImageIfPresent(Post.class, previewImage);
    // DB 작업 처리 + 실패시 파일 롤백
    try {
      String oldImageName = themeBoardTransactionService.updateThemeBoardAndGetOldFileName(
          postId,
          updateDto,
          newImageName
      );
      if (newImageName != null && oldImageName != null) {
        fileUtils.deleteFileSilently(oldImageName, "ThemeBoard의 이전 파일 삭제 실패");
      }
    } catch (Exception e) {
      fileUtils.deleteFileSilently(newImageName, "ThemeBoard 갱신 실패로 인한 파일 롤백 실패");
      throw e;
    }
    return themeBoardTransactionService.findThemeBoardDetail(postId, userIdentifier);
  }

  /**
   * 테마 게시글 삭제
   *
   * @param postId 테마 게시글 ID
   */
  @Transactional
  public void deleteThemeBoard(Long postId) {
    Post targetPost = postService.getPostByPostId(postId);
    themeBoardService.deleteByPostId(postId);
    postService.deletePost(postId);
    // 기존 이미지 삭제 작업 시도 ( 실패 허용 )
    fileUtils.deleteFileSilently(targetPost.getPreviewImageName(),
        "게시글 삭제 시 대표 이미지 삭제 실패");
  }
}
