package com.komentum.post.service.transaction;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.post.mapper.DesignBoardMapperSupport;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.DesignBoardRepositorySupport;
import com.komentum.post.service.DesignBoardService;
import com.komentum.post.service.PostService;
import com.komentum.post.service.TagService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignBoardTransactionService {

  private final UserEntityFinder userEntityFinder;
  private final DesignBoardRepositorySupport designBoardRepositorySupport;
  private final DesignBoardMapperSupport designBoardMapperSupport;
  private final BoardManagementHelper helper;
  private final PostDtoMapper postDtoMapper;
  private final PostService postService;
  private final DesignBoardService designBoardService;
  private final TagService tagService;
  private final DesignBoardRepository designBoardRepository;

  /**
   * 활성 에셋이 남은 디자인 게시글의 상세 응답을 이미지·타입·태그와 함께 조립한다.
   *
   * @param userIdentifier 사용자 식별자 (비인증 요청은 null)
   * @throws ResourceNotFoundException 게시글 연결이 없거나 활성 에셋이 하나도 없는 경우
   */
  @Transactional(readOnly = true)
  public DesignBoardDetailDto findDesignBoardDetail(Long postId, String userIdentifier) {
    if (!designBoardRepository.existsByPost_PostId(postId)) {
      throw new ResourceNotFoundException("Design board not found with post id: " + postId);
    }
    User client = userIdentifier == null ? null : userEntityFinder.findUserEntity(userIdentifier);
    DesignBoardQuery.Detail detail = designBoardRepositorySupport
        .findDetailByPostId(postId, client);
    // 상세 쿼리는 활성 에셋만 대상으로 하므로 기존 연결이 있어도 모두 비활성이면 조회되지 않는다.
    if (detail == null) {
      throw new ResourceNotFoundException("Design board not found with post id: " + postId);
    }
    List<Tag> tags = tagService.findAllByPostId(postId);
    // 대표 이미지 대체 여부는 비활성을 포함한 전체 연결로 판단하고 에셋 이미지는 활성만 노출한다.
    Post targetPost = postService.getPostByPostId(postId);
    List<DesignBoard> designBoards = designBoardService.findWithDesignComponentsByPostId(postId);
    String previewImageUrl =
        helper.findPreviewImageUrl(designBoardService.resolveVisiblePreviewImageName(
            targetPost.getPreviewImageName(), designBoards));
    List<String> designImageUrls = designBoards.stream()
        .filter(board -> Boolean.TRUE.equals(board.getDesignComponent().getIsActive()))
        .map(designBoard -> helper.findPreviewImageUrl(
            designBoard.getDesignComponent().getFileName()))
        .toList();
    List<String> previewImageUrls = Stream.concat(
            Stream.ofNullable(previewImageUrl),
            designImageUrls.stream()
        )
        .filter(Objects::nonNull)
        .toList();
    // 타입도 활성 에셋만 대상으로 조립해 이미지와 동일한 노출 정책을 적용한다.
    return designBoardMapperSupport.toDesignBoardDetailDto(
        detail,
        tags,
        previewImageUrls,
        designBoardMapperSupport.toComponentTypeDtos(designBoards));
  }

  @Transactional
  public Post saveDesignBoardAndGetPost(
      DesignBoardCreateDto createDto,
      List<DesignComponent> designComponents,
      User author,
      String previewImageName) {
    PostCreateDto postCreateDto = postDtoMapper.toPostCreateDto(createDto);
    Post savedPost = postService
        .createPost(postCreateDto, author, previewImageName, PostType.DESIGN_BOARD);
    for (DesignComponent designComponent : designComponents) {
      designBoardService.save(savedPost, designComponent);
    }
    if (createDto.getPostTags() != null) {
      tagService.createTags(savedPost, createDto.getPostTags());
    }
    return savedPost;
  }

  /**
   * 게시글·태그·사전 검증한 에셋 연결을 갱신하고 파일 정리에 사용할 이전 대표 이미지명을 반환한다.
   * 연결 목록이 null이거나 비어 있으면 기존 연결을 유지하며 파일 저장·삭제는 호출 측에 맡긴다.
   *
   * @throws ResourceNotFoundException 게시글의 에셋 연결이 없는 경우
   */
  @Transactional
  public String updateDesignBoardAndGetOldFileName(
      Long postId,
      DesignBoardUpdateDto updateDto,
      String previewImageName,
      List<DesignComponent> requestedComponents
  ) {
    if (!designBoardRepository.existsByPost_PostId(postId)) {
      throw new ResourceNotFoundException("Design board not found with post id: " + postId);
    }
    // update post info
    PostUpdateDto postUpdateDto = postDtoMapper.toPostUpdateDto(updateDto, previewImageName);
    Post targetPost = postService.getPostByPostId(postId);
    String oldFileName = targetPost.getPreviewImageName();
    targetPost.update(postUpdateDto);
    // update tags
    if (updateDto.getPostTags() != null) {
      tagService.synchronizeTags(targetPost, updateDto.getPostTags());
    }
    // synchronize design boards
    if (requestedComponents != null && !requestedComponents.isEmpty()) {
      designBoardService.synchronizeDesignBoards(targetPost, requestedComponents);
    }
    // retrieve old file name
    return oldFileName;
  }
}
