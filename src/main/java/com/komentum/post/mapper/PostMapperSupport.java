package com.komentum.post.mapper;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.dto.query.PostQuery;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.post.service.DesignBoardService;
import com.komentum.post.service.TagService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostMapperSupport {

  private final BoardManagementHelper helper;
  private final TagService tagService;
  private final DesignBoardService designBoardService;

  /**
   * 사용자 게시글 목록에 태그를 결합하고 디자인 게시글의 대표 이미지에 활성 에셋 정책을 적용한다.
   * 테마 게시글은 과거 에셋 참조를 보존하므로 기존 대표 이미지를 그대로 사용한다.
   */
  @Transactional(readOnly = true)
  public List<UserPostListResponseDto> toUserPostListResponseDtoList(
      List<PostQuery.UserPostListRow> rows) {
    if (rows.isEmpty()) {
      return List.of();
    }
    List<Long> postIds = rows.stream().map(PostQuery.UserPostListRow::getPostId).toList();
    Map<Long, List<Tag>> tagMap = tagService.getTagPerPosts(postIds);
    List<Long> designPostIds = rows.stream()
        .filter(row -> row.getPostType() == PostType.DESIGN_BOARD)
        .map(PostQuery.UserPostListRow::getPostId)
        .toList();
    Map<Long, List<DesignBoard>> designBoardsByPostId =
        designBoardService.findWithDesignComponentsByPostIdMap(designPostIds);
    return rows.stream().map(row -> {
      Long postId = row.getPostId();
      String previewImageName = row.getPostType() == PostType.DESIGN_BOARD
          ? designBoardService.resolveVisiblePreviewImageName(
              row.getPreviewImageName(), designBoardsByPostId.getOrDefault(postId, List.of()))
          : row.getPreviewImageName();
      return UserPostListResponseDto.from(row, tagMap.getOrDefault(postId, List.of()), helper,
          previewImageName);
    }).toList();
  }
}
