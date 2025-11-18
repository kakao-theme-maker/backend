package com.komentum.post.facade;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.repository.PostRepositorySupport;
import com.komentum.post.service.DesignBoardService;
import com.komentum.post.service.PostService;
import com.komentum.post.service.TagService;
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
  private final TagService tagService;
  private final DesignBoardService designBoardService;
  private final UserRetrieveService userRetrieveService;
  private final BoardManagementHelper boardManagementHelper;

  // mapper
  private final PostDtoMapper postDtoMapper;

  @Transactional(readOnly = true)
  public DesignBoardDetailDto findBoardDetail(Long boardId) {
    PostSummary postSummary = postRepositorySupport.findPostSummaryByPostId(boardId);
    DesignBoard designBoard = designBoardService.findByPostId(boardId);
    List<Tag> tags = tagService.getTagsByPostId(boardId);
    String profileImageUrl = boardManagementHelper.findProfileImageUrl(
        postSummary.findProfileImageName());
    return DesignBoardDetailDto.from(postSummary.getPost(),
        designBoard.getDesignComponent(), postSummary.getAuthor(), tags,
        postSummary.getPrefers(), profileImageUrl);
  }

  @Transactional(readOnly = true)
  public List<DesignBoardPreviewDto> findBoardPreviews(Pageable pageable) {
    List<PostSummary> postSummaries = postRepositorySupport.findPostSummaries(pageable);
    List<Long> postIds = postSummaries.stream().map(PostSummary::findPostId).toList();
    Map<Long, DesignBoard> postBoardDetailMap = designBoardService.findAllByPostIds(postIds)
        .stream()
        .collect(Collectors.toMap(DesignBoard::findPostId, Function.identity()));
    return postSummaries.stream().map(postSummary -> {
      DesignBoard designBoard = postBoardDetailMap.get(postSummary.findPostId());
      String profileImageUrl = boardManagementHelper.findProfileImageUrl(
          postSummary.findProfileImageName());
      return DesignBoardPreviewDto.from(postSummary.getPost(),
          designBoard.getDesignComponent(), postSummary.getAuthor(),
          postSummary.getPrefers(), profileImageUrl);
    }).toList();
  }

  @Transactional
  public DesignBoardDetailDto createBoardWithTags(
      DesignBoardCreateDto createDto, MultipartFile profileImage) {
    User author = userRetrieveService.findUserEntity(createDto.getUserEmail());
    Post savedPost = boardManagementHelper.createPostAndProfileImage(
        postDtoMapper.toPostCreateDto(createDto), author, profileImage);
    tagService.createTags(savedPost, createDto.getPostTags());
    DesignComponent designComponent = designComponentService.getEntityById(
        createDto.getDesignComponentId());
    designBoardService.save(savedPost, designComponent);
    return findBoardDetail(savedPost.getPostId());
  }

  @Transactional
  public DesignBoardDetailDto updateBoardWithTags(long boardId,
      DesignBoardUpdateDto updateDto) {
    User editor = userRetrieveService.findUserEntity(updateDto.getUserEmail());
    Post updatedPost = postService.updatePost(boardId, editor,
        postDtoMapper.toPostUpdateDto(updateDto));
    tagService.synchronizeTags(updatedPost, updateDto.getPostTags());
    return findBoardDetail(boardId);
  }

  @Transactional
  public void deleteBoardDetailWithPost(Long boardId) {
    designBoardService.deleteByPostId(boardId);
    postService.deletePost(boardId);
  }
}
