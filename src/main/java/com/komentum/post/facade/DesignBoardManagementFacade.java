package com.komentum.post.facade;

import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.dto.query.DesignBoardQuery.Detail;
import com.komentum.post.mapper.DesignBoardMapperSupport;
import com.komentum.post.service.DesignBoardService;
import com.komentum.post.service.PostService;
import com.komentum.post.service.TagService;
import com.komentum.post.service.transaction.DesignBoardTransactionService;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignBoardManagementFacade {

  private final DesignComponentService designComponentService;
  private final PostService postService;
  private final DesignBoardService designBoardService;
  private final UserEntityFinder userEntityFinder;
  private final BoardManagementHelper boardManagementHelper;
  private final FileManager fileManager;
  private final DesignBoardMapperSupport designBoardMapperSupport;
  private final DesignBoardTransactionService designBoardTransactionService;
  private final TagService tagService;

  private String uploadOrReusePreviewImage(
      MultipartFile previewImage,
      DesignComponent designComponent
  ) {
    if (previewImage == null || previewImage.isEmpty()) {
      String fileName = fileManager.convertUrlToFileName(designComponent.getImageUrl());
      byte[] previewImageBytes = fileManager.downloadFile(fileName);
      return boardManagementHelper.savePreviewImageIfPresent(Post.class, fileName,
          previewImageBytes);
    }
    return boardManagementHelper.savePreviewImageIfPresent(Post.class, previewImage);
  }

  /**
   * 게시글 ID 기반으로 디자인 에셋 게시글 상세 조회
   * @param postId 게시글 ID
   * @return 특정 post id를 갖는 디자인 에셋 게시글 상세 정보
   * */
  @Transactional(readOnly = true)
  public DesignBoardDetailDto findBoardDetail(Long postId, String userIdentifier) {
    return designBoardTransactionService.findDesignBoardDetail(postId, userIdentifier);
  }

  /**
   * 디자인 에셋 게시글을 페이지 기반 조회
   * @param pageable 게시글 페이지 번호와 크기 정보
   * @return 디자인 에셋 게시글 정보 목록 반환
   * */
  @Transactional(readOnly = true)
  public List<DesignBoardPreviewDto> findBoardPreviews(Pageable pageable) {
    List<DesignBoardQuery.Preview> preview = designBoardService.findPreviewList(pageable);
    return preview.stream()
        .map(p ->
            designBoardMapperSupport.toDesignBoardPreviewDto(p, boardManagementHelper))
        .toList();
  }

  /**
   * 디자인 에셋 게시글 상세 조회 시 연관 디자인 에셋 게시글을 함께 제공한다
   * @param pageable 페이징 정보
   * @param pinnedPostId page=0일 때, 위에 고정할 게시글 정보
   * @param userIdentifier 사용자 식별자
   * @return 디자인 에셋 상세 정보 목록
   * */
  @Transactional(readOnly = true)
  public List<DesignBoardDetailDto> findBoardDetails(Pageable pageable, Long pinnedPostId,
      String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    Post pinnedPost = pinnedPostId == null ?
        null :
        postService.findByPostIdAndPostType(pinnedPostId, PostType.DESIGN_BOARD);
    // 디자인 에셋 상세 정보 DTO Projection
    List<DesignBoardQuery.Detail> details = designBoardService.findDetailList(pageable, client,
        pinnedPost);
    // post id 추출
    List<Long> postIds = details.stream()
        .map(Detail::getPostId)
        .toList();
    // post id별 design board 목록 조회
    Map<Long, List<DesignBoard>> designBoardMap = designBoardService.findWithDesignComponentsByPostIdIn(
            postIds)
        .stream()
        .collect(Collectors.groupingBy(
            designBoard -> designBoard.getPost().getPostId()
        ));
    // post id별 tag 목록 조호
    Map<Long, List<Tag>> tagMap = tagService.getTagPerPosts(postIds);
    return details.stream().map(detail -> {
      List<Tag> tags = tagMap.getOrDefault(detail.getPostId(), List.of());
      List<DesignBoard> designBoards = designBoardMap.getOrDefault(detail.getPostId(), List.of());
      List<String> previewImageUrls = designBoards.stream()
          .map(designBoard -> designBoard.getDesignComponent().getImageUrl())
          .toList();
      return designBoardMapperSupport.toDesignBoardDetailDto(detail, tags, previewImageUrls);
    }).toList();
  }

  /**
   * <p>디자인 에셋들에 대한 디자인 에셋 게시글을 생성한다</p>
   * <p>디자인 에셋 생성은 별도 디자인 에셋 생성 API에서 처리한다 </p>
   * @param createDto 게시글 생성에 필요한 정보
   * @param authorId 게시글 작성자 ID
   * @return 생성된 게시글 상세 정보
   * */
  @Transactional
  public DesignBoardDetailDto createDesignBoard(DesignBoardCreateDto createDto,
      MultipartFile previewImage, String authorId) {
    // 이미지 저장
    List<DesignComponent> designComponents = designComponentService.findByIdIn(
        createDto.getDesignComponentIds());
    String previewImageName = uploadOrReusePreviewImage(previewImage, designComponents.get(0));
    // DB 작업 수행
    User author = userEntityFinder.findUserEntity(authorId);
    try {
      Post savedPost = designBoardTransactionService.saveDesignBoardAndGetPost(
          createDto,
          designComponents,
          author,
          previewImageName
      );
      return designBoardTransactionService.findDesignBoardDetail(savedPost.getPostId(), authorId);
    } catch (Exception e) {
      boardManagementHelper.deleteFileSilently(previewImageName, "디자인 게시글 생성 실패로 인한 저장된 파일 롤백");
      log.error(e.getMessage(), e);
      throw new RuntimeException("디자인 에셋 게시글 생성 실패", e);
    }
  }

  /**
   * 디자인 에셋 게시글 수정
   * @param postId 게시글 ID
   * @param updateDto 게시글 수정 DTO
   * */
  public DesignBoardDetailDto updateDesignBoard(
      Long postId,
      DesignBoardUpdateDto updateDto,
      MultipartFile previewImage,
      String userIdentifier) {
    // DB 쓰기 작업보다 파일 작업을 먼저 처리해야하므로 파일 작업 우선 처리
    String newImageName = boardManagementHelper.savePreviewImageIfPresent(DesignComponent.class,
        previewImage);
    // DB 작업 커밋 + 기존 이미지 삭제
    try {
      String oldImageName = designBoardTransactionService.updateDesignBoardAndGetOldFileName(
          postId,
          updateDto,
          newImageName
      );
      if (newImageName != null && oldImageName != null) {
        boardManagementHelper.deleteFileSilently(oldImageName, "Design Board의 이전 파일 삭제 실패");
      }
    } catch (Exception e) {
      boardManagementHelper.deleteFileSilently(newImageName, "Design Board 갱신 실패로 인한 파일 롤백 실패");
      throw e;
    }
    // 응답값 반환
    return designBoardTransactionService.findDesignBoardDetail(postId, userIdentifier);
  }

  /**
   * 디자인 에셋 게시글 삭제
   * @param postId 게시글 ID
   * */
  @Transactional
  public void deleteBoardDetailWithPost(Long postId) {
    Post targetPost = postService.getPostByPostId(postId);
    designBoardService.deleteByPostId(postId);
    postService.deletePost(postId);
    // 기존 이미지 삭제 작업 시도 ( 실패 허용 )
    boardManagementHelper.deleteFileSilently(targetPost.getPreviewImageName(),
        "게시글 삭제 시 대표 이미지 삭제 실패");
  }
}
