package com.komentum.post.facade;

import com.komentum.global.utils.FileManager;
import com.komentum.global.exception.ResourceNotFoundException;
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
import com.komentum.post.service.enums.PostSortType;
import com.komentum.post.service.transaction.DesignBoardTransactionService;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.service.DesignComponentService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  /**
   * 업로드 이미지가 없으면 에셋 원본의 바이트를 복사해 게시글 전용 대표 이미지로 저장한다.
   * 에셋과 파일명을 공유하지 않아 게시글 이미지 교체·삭제가 원본 파일에 영향을 주지 않는다.
   */
  private String uploadOrReusePreviewImage(
      MultipartFile previewImage,
      DesignComponent designComponent
  ) {
    if (previewImage == null || previewImage.isEmpty()) {
      String fileName = designComponent.getFileName();
      byte[] previewImageBytes = fileManager.downloadFile(fileName);
      return boardManagementHelper.savePreviewImageIfPresent(Post.class, fileName,
          previewImageBytes);
    }
    return boardManagementHelper.savePreviewImageIfPresent(Post.class, previewImage);
  }

  /**
   * 게시글 ID 기반으로 디자인 에셋 게시글 상세 조회
   * @param postId 게시글 ID
   * @param userIdentifier 사용자 식별자 (비인증 요청은 null)
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
    return findBoardPreviews(pageable, null, null, PostSortType.CREATED_DESC);
  }

  @Transactional(readOnly = true)
  public List<DesignBoardPreviewDto> findBoardPreviews(Pageable pageable, String keyword,
      TypeCode typeCode) {
    return findBoardPreviews(pageable, keyword, typeCode, PostSortType.CREATED_DESC);
  }

  /**
   * 활성 에셋 연결 단위의 페이지를 조회하고 노출 가능한 대표 이미지·타입을 조립한다.
   * 같은 게시글에 활성 에셋이 여러 개면 게시글 ID가 같은 미리보기가 여러 행 반환된다.
   */
  @Transactional(readOnly = true)
  public List<DesignBoardPreviewDto> findBoardPreviews(Pageable pageable, String keyword,
      TypeCode typeCode, PostSortType sortType) {
    List<DesignBoardQuery.Preview> preview = designBoardService.findPreviewList(pageable, keyword,
        typeCode, sortType);
    List<Long> postIds = preview.stream()
        .map(DesignBoardQuery.Preview::getPostId)
        .toList();
    Map<Long, List<DesignBoard>> designBoardMap =
        designBoardService.findWithDesignComponentsByPostIdMap(postIds);
    return preview.stream()
        .map(p ->
            designBoardMapperSupport.toDesignBoardPreviewDto(p, boardManagementHelper,
                designBoardMapperSupport.toComponentTypeDtos(
                    designBoardMap.getOrDefault(p.getPostId(), List.of())),
                designBoardService.resolveVisiblePreviewImageName(p.getPreviewImageName(),
                    designBoardMap.getOrDefault(p.getPostId(), List.of()))))
        .toList();
  }

  /**
   * 디자인 에셋 게시글 상세 조회 시 연관 디자인 에셋 게시글을 함께 제공한다
   * @param pageable 페이징 정보
   * @param pinnedPostId page=0일 때, 위에 고정할 게시글 정보
   * @param userIdentifier 사용자 식별자 (비인증 요청은 null)
   * @return 디자인 에셋 상세 정보 목록
   * */
  @Transactional(readOnly = true)
  public List<DesignBoardDetailDto> findBoardDetails(Pageable pageable, Long pinnedPostId,
      String userIdentifier) {
    User client = userIdentifier == null ? null : userEntityFinder.findUserEntity(userIdentifier);
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
    Map<Long, List<DesignBoard>> designBoardMap =
        designBoardService.findWithDesignComponentsByPostIdMap(postIds);
    // post id별 tag 목록 조회
    Map<Long, List<Tag>> tagMap = tagService.getTagPerPosts(postIds);
    return details.stream().map(detail -> {
      List<Tag> tags = tagMap.getOrDefault(detail.getPostId(), List.of());
      List<DesignBoard> designBoards = designBoardMap.getOrDefault(detail.getPostId(), List.of());
      List<String> previewImageUrls = resolvePreviewImageUrls(detail, designBoards);
      return designBoardMapperSupport.toDesignBoardDetailDto(detail, tags, previewImageUrls,
          designBoardMapperSupport.toComponentTypeDtos(designBoards));
    }).toList();
  }

  /**
   * 노출 정책에 맞춘 대표 이미지 뒤에 활성 에셋 이미지들을 연결해 상세 이미지 목록을 만든다.
   * 대표 이미지 대체 여부를 판단할 수 있도록 입력에는 비활성 연결도 포함한다.
   */
  private List<String> resolvePreviewImageUrls(DesignBoardQuery.Detail detail,
      List<DesignBoard> designBoards) {
    String postPreviewImageUrl = boardManagementHelper.findPreviewImageUrl(
        designBoardService.resolveVisiblePreviewImageName(detail.getPostPreviewImageName(),
            designBoards));
    return Stream.concat(
        Stream.ofNullable(postPreviewImageUrl),
        designBoards.stream()
            .filter(board -> Boolean.TRUE.equals(board.getDesignComponent().getIsActive()))
            .map(designBoard -> boardManagementHelper.findPreviewImageUrl(
                designBoard.getDesignComponent().getFileName()))
    ).toList();
  }

  /**
   * 기존 활성 에셋에 게시글을 연결하고 대표 이미지·태그를 저장한다.
   * 에셋 자체는 생성하지 않으며, 이미지 저장 전에 신규 연결의 활성 여부를 검증한다.
   *
   * @throws ResourceNotFoundException 요청한 에셋이 없거나 비활성인 경우
   * @throws RuntimeException 게시글 생성에 실패해 저장한 대표 이미지의 정리를 시도한 경우
   */
  @Transactional
  public DesignBoardDetailDto createDesignBoard(DesignBoardCreateDto createDto,
      MultipartFile previewImage, String authorId) {
    // 비활성 에셋을 새 게시글에 연결할 수 없으므로 파일 저장 전에 검사한다.
    List<DesignComponent> designComponents = designComponentService.findByIdIn(
        createDto.getDesignComponentIds());
    designComponentService.requireActiveComponents(designComponents);
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
   * 기존 연결 보존 정책에 따라 에셋 연결·게시글·태그·대표 이미지를 수정한다.
   * 연결 ID 목록이 null이거나 비어 있으면 기존 연결을 유지한다.
   *
   * @throws ResourceNotFoundException 게시글 연결이 없거나 요청 에셋이 없거나,
   *     새 연결에 비활성 에셋이 포함되거나 수정 결과에 활성 에셋이 하나도 없는 경우
   */
  public DesignBoardDetailDto updateDesignBoard(
      Long postId,
      DesignBoardUpdateDto updateDto,
      MultipartFile previewImage,
      String userIdentifier) {
    List<DesignBoard> existingBoards = designBoardService.findWithDesignComponentsByPostId(postId);
    if (existingBoards.isEmpty()) {
      throw new ResourceNotFoundException("Design board not found with post id: " + postId);
    }
    List<DesignComponent> requestedComponents = null;
    if (updateDto.getDesignComponentIds() != null && !updateDto.getDesignComponentIds().isEmpty()) {
      requestedComponents = designComponentService.findByIdIn(updateDto.getDesignComponentIds());
      Set<Integer> existingIds = existingBoards
          .stream().map(board -> board.getDesignComponent().getDesignComponentId())
          .collect(Collectors.toSet());
      // 기존 연결 ID는 비활성이어도 유지할 수 있지만 새로 연결하는 에셋은 활성이어야 한다.
      designComponentService.requireActiveComponents(requestedComponents.stream()
          .filter(component -> !existingIds.contains(component.getDesignComponentId()))
          .toList());
    }
    List<DesignComponent> visibleComponents = requestedComponents == null
        ? existingBoards.stream().map(DesignBoard::getDesignComponent).toList()
        : requestedComponents;
    // 조회할 수 없는 결과를 만들지 않도록 파일 업로드와 DB 변경 전에 거절한다.
    if (visibleComponents.stream()
        .noneMatch(component -> Boolean.TRUE.equals(component.getIsActive()))) {
      throw new ResourceNotFoundException("Design board not found with post id: " + postId);
    }
    // DB 쓰기 작업보다 파일 작업을 먼저 처리해야하므로 파일 작업 우선 처리
    String newImageName = boardManagementHelper.savePreviewImageIfPresent(DesignComponent.class,
        previewImage);
    // 비활성 연결을 모두 제거하면 기존 합성 이미지가 다시 노출되므로 활성 에셋의 별도 복사본으로 교체한다.
    if (newImageName == null && requestedComponents != null
        && existingBoards.stream()
            .anyMatch(board -> !Boolean.TRUE.equals(board.getDesignComponent().getIsActive()))
        && requestedComponents.stream()
            .allMatch(component -> Boolean.TRUE.equals(component.getIsActive()))) {
      // 요청 목록의 순서와 관계없이 같은 에셋을 고르도록 최소 ID를 사용한다.
      DesignComponent previewComponent = requestedComponents.stream()
          .min(Comparator.comparing(DesignComponent::getDesignComponentId)).orElseThrow();
      newImageName = uploadOrReusePreviewImage(null, previewComponent);
    }
    // DB 작업 커밋 + 기존 이미지 삭제
    try {
      String oldImageName = designBoardTransactionService.updateDesignBoardAndGetOldFileName(
          postId,
          updateDto,
          newImageName,
          requestedComponents
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
