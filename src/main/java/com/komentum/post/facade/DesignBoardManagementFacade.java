package com.komentum.post.facade;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.repository.PostRepositorySupport;
import com.komentum.post.service.DesignBoardService;
import com.komentum.post.service.PostService;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.service.DesignComponentService;
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

@Service
@RequiredArgsConstructor
public class DesignBoardManagementFacade {

  private final DesignComponentService designComponentService;
  private final PostService postService;
  private final PostRepositorySupport postRepositorySupport;
  private final DesignBoardService designBoardService;
  private final UserRetrieveService userRetrieveService;
  private final BoardManagementHelper boardManagementHelper;

  // mapper
  private final PostDtoMapper postDtoMapper;

  /**
   * 게시글 ID 기반으로 디자인 에셋 게시글 상세 조회
   * @param postId 게시글 ID
   * @return 특정 post id를 갖는 디자인 에셋 게시글 상세 정보
   * */
  @Transactional(readOnly = true)
  public DesignBoardDetailDto findBoardDetail(Long postId) {
    PostSummary postSummary = postRepositorySupport.findPostSummaryByPostId(postId);
    DesignBoard designBoard = designBoardService.findByPostId(postId);
    String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
        postSummary.findPreviewImageName());
    return DesignBoardDetailDto.from(postSummary.getPost(),
        designBoard.getDesignComponent(), postSummary.getAuthor(),
        postSummary.getPrefers(), previewImageUrl);
  }

  /**
   * 디자인 에셋 게시글을 페이지 기반 조회
   * @param pageable 게시글 페이지 번호와 크기 정보
   * @return 디자인 에셋 게시글 정보 목록 반환
   * */
  @Transactional(readOnly = true)
  public List<DesignBoardPreviewDto> findBoardPreviews(Pageable pageable) {
    List<PostSummary> postSummaries = postRepositorySupport.findPostSummaries(pageable);
    List<Long> postIds = postSummaries.stream().map(PostSummary::findPostId).toList();
    Map<Long, DesignBoard> postBoardDetailMap = designBoardService.findAllByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(DesignBoard::findPostId, Function.identity()));
    return postSummaries.stream().map(postSummary -> {
      DesignBoard designBoard = postBoardDetailMap.get(postSummary.findPostId());
      String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
          postSummary.findPreviewImageName());
      return DesignBoardPreviewDto.from(postSummary.getPost(),
          designBoard.getDesignComponent(), postSummary.getAuthor(),
          postSummary.getPrefers(), previewImageUrl);
    }).toList();
  }

  /**
   * 디자인 에셋 게시글 생성
   * @param createDto 게시글 생성에 필요한 정보
   * @param previewImage 게시글 대표 이미지, null=true
   * @param authorId 게시글 작성자 ID
   * @return 생성된 게시글 상세 정보
   * */
  @Transactional
  public DesignBoardDetailDto createDesignBoard(
      DesignBoardCreateDto createDto, MultipartFile previewImage, String authorId) {
    User author = userRetrieveService.findUserEntity(authorId);
    Post savedPost = boardManagementHelper.createPostAndPreviewImage(
        postDtoMapper.toPostCreateDto(createDto), author, previewImage);
    DesignComponent designComponent = designComponentService.getEntityById(
        createDto.getDesignComponentId());
    designBoardService.save(savedPost, designComponent);
    return findBoardDetail(savedPost.getPostId());
  }

  /**
   * 디자인 에셋 게시글 수정
   * @param postId 게시글 ID
   * @param updateDto 게시글 수정 DTO
   * */
  @Transactional
  public DesignBoardDetailDto updateDesignBoard(Long postId,
      DesignBoardUpdateDto updateDto) {
    postService.updatePost(postId,
        postDtoMapper.toPostUpdateDto(updateDto));
    return findBoardDetail(postId);
  }

  /**
   * 디자인 에셋 게시글 삭제
   * @param postId 게시글 ID
   * */
  @Transactional
  public void deleteBoardDetailWithPost(Long postId) {
    designBoardService.deleteByPostId(postId);
    postService.deletePost(postId);
  }
}
