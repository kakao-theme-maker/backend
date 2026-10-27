package com.komentum.post.service.transaction;

import com.komentum.designcomponent.domain.DesignComponent;
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
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
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
   * 게시글 ID로 디자인 에셋 게시글 상세 정보를 조회한다.
   *
   * @param postId 게시글 ID
   * @param userIdentifier 사용자 식별자 (비인증 요청은 null)
   * @return 디자인 에셋 게시글 상세 정보
   */
  @Transactional(readOnly = true)
  public DesignBoardDetailDto findDesignBoardDetail(Long postId, String userIdentifier) {
    if (!designBoardRepository.existsByPost_PostId(postId)) {
      throw new EntityNotFoundException("cannot find design board with post id = " + postId);
    }
    User client = userIdentifier == null ? null : userEntityFinder.findUserEntity(userIdentifier);
    DesignBoardQuery.Detail detail = designBoardRepositorySupport
        .findDetailByPostId(postId, client);
    List<Tag> tags = tagService.findAllByPostId(postId);
    // generate preview image url list
    Post targetPost = postService.getPostByPostId(postId);
    String previewImageUrl =
        helper.findPreviewImageUrl(targetPost.getPreviewImageName());
    List<DesignBoard> designBoards = designBoardService.findWithDesignComponentsByPostId(postId);
    List<String> designImageUrls = designBoards.stream()
        .map(DesignBoard::getDesignComponent)
        .collect(Collectors.toMap(DesignComponent::getDesignComponentId,
            Function.identity(), (first, ignored) -> first, LinkedHashMap::new))
        .values().stream()
        .map(component -> helper.findPreviewImageUrl(component.getFileName()))
        .toList();
    List<String> previewImageUrls = Stream.concat(
            Stream.ofNullable(previewImageUrl),
            designImageUrls.stream()
        )
        .filter(Objects::nonNull)
        .toList();
    // generate response dto
    DesignBoardDetailDto response = designBoardMapperSupport.toDesignBoardDetailDto(
        detail,
        tags,
        previewImageUrls,
        designBoardMapperSupport.toComponentTypeDtos(designBoards));
    response.setDesignComponents(designBoardMapperSupport.toDesignComponentMappings(designBoards));
    return response;
  }

  /**
   * 검증된 에셋별 타입 매핑과 태그를 포함하여 디자인 게시글을 생성한다.
   */
  @Transactional
  public Post saveDesignBoardAndGetPost(
      DesignBoardCreateDto createDto,
      List<DesignBoard> designBoards,
      User author,
      String previewImageName) {
    PostCreateDto postCreateDto = postDtoMapper.toPostCreateDto(createDto);
    Post savedPost = postService
        .createPost(postCreateDto, author, previewImageName, PostType.DESIGN_BOARD);
    for (DesignBoard designBoard : designBoards) {
      designBoardService.save(savedPost, designBoard);
    }
    if (createDto.getPostTags() != null) {
      tagService.createTags(savedPost, createDto.getPostTags());
    }
    return savedPost;
  }

  /**
   * 게시글과 태그 및 요청된 매핑을 한 트랜잭션에서 수정하고 이전 대표 이미지명을 반환한다.
   */
  @Transactional
  public String updateDesignBoardAndGetOldFileName(
      Long postId,
      DesignBoardUpdateDto updateDto,
      String previewImageName,
      List<DesignBoard> requestedMappings
  ) {
    if (!designBoardRepository.existsByPost_PostId(postId)) {
      throw new EntityNotFoundException("cannot find design board with post id = " + postId);
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
    if (requestedMappings != null) {
      designBoardService.synchronizeDesignBoards(targetPost, requestedMappings);
    }
    // retrieve old file name
    return oldFileName;
  }
}
