package com.komentum.post.service.transaction;

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
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
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

  @Transactional(readOnly = true)
  public DesignBoardDetailDto findDesignBoardDetail(Long postId, String userIdentifier) {
    if (!designBoardRepository.existsByPost_PostId(postId)) {
      throw new EntityNotFoundException("cannot find design board with post id = " + postId);
    }
    User client = userEntityFinder.findUserEntity(userIdentifier);
    DesignBoardQuery.Detail detail = designBoardRepositorySupport
        .findDetailByPostId(postId, client);
    List<Tag> tags = tagService.findAllByPostId(postId);
    return designBoardMapperSupport.toDesignBoardDetailDto(
        detail,
        helper,
        tags);
  }

  @Transactional
  public Post saveDesignBoardAndGetPost(
      DesignBoardCreateDto createDto,
      DesignComponent designComponent,
      User author,
      String previewImageName) {
    PostCreateDto postCreateDto = postDtoMapper.toPostCreateDto(createDto);
    Post savedPost = postService
        .createPost(postCreateDto, author, previewImageName, PostType.DESIGN_BOARD);
    designBoardService.save(savedPost, designComponent);
    if (createDto.getPostTags() != null) {
      tagService.createTags(savedPost, createDto.getPostTags());
    }
    return savedPost;
  }

  @Transactional
  public String updateDesignBoardAndGetOldFileName(
      Long postId,
      DesignBoardUpdateDto updateDto,
      String previewImageName
  ) {
    if (!designBoardRepository.existsByPost_PostId(postId)) {
      throw new EntityNotFoundException("cannot find design board with post id = " + postId);
    }
    PostUpdateDto postUpdateDto = postDtoMapper.toPostUpdateDto(updateDto, previewImageName);
    Post targetPost = postService.getPostByPostId(postId);
    String oldFileName = targetPost.getPreviewImageName();
    targetPost.update(postUpdateDto);
    if (updateDto.getPostTags() != null) {
      tagService.synchronizeTags(targetPost, updateDto.getPostTags());
    }
    return oldFileName;
  }
}
