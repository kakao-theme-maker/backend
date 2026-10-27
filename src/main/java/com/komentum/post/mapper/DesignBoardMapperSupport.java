package com.komentum.post.mapper;

import com.komentum.global.utils.DateUtils;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.BoardComponentTypeDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignComponentMappingDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.facade.BoardManagementHelper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DesignBoardMapperSupport {

  /**
   * 게시글의 타입 목록을 중복 없이 응답 DTO로 변환한다.
   */
  public List<BoardComponentTypeDto> toComponentTypeDtos(List<DesignBoard> designBoards) {
    Map<TypeCode, BoardComponentTypeDto> componentTypeMap = new LinkedHashMap<>();
    for (DesignBoard designBoard : designBoards) {
      ComponentType componentType = designBoard.getComponentType();
      componentTypeMap.putIfAbsent(componentType.getTypeCode(),
          BoardComponentTypeDto.from(componentType));
    }
    return List.copyOf(componentTypeMap.values());
  }

  /**
   * 게시글 매핑을 에셋별 타입 ID 목록으로 묶어 반환한다.
   */
  public List<DesignComponentMappingDto> toDesignComponentMappings(List<DesignBoard> designBoards) {
    Map<Integer, List<Integer>> mappings = new LinkedHashMap<>();
    for (DesignBoard designBoard : designBoards) {
      Integer designComponentId = designBoard.getDesignComponent().getDesignComponentId();
      mappings.computeIfAbsent(designComponentId, id -> new ArrayList<>())
          .add(designBoard.getComponentType().getComponentTypeId());
    }
    return mappings.entrySet().stream()
        .map(entry -> DesignComponentMappingDto.builder()
            .designComponentId(entry.getKey())
            .componentTypeIds(List.copyOf(entry.getValue()))
            .build())
        .toList();
  }

  public DesignBoardPreviewDto toDesignBoardPreviewDto(
      DesignBoardQuery.Preview preview,
      BoardManagementHelper helper,
      List<BoardComponentTypeDto> componentTypes
  ) {
    return DesignBoardPreviewDto.builder()
        .postId(preview.getPostId())
        .title(preview.getTitle())
        .previewImageUrl(helper.findPreviewImageUrl(preview.getPreviewImageName()))
        .userEmail(preview.getUserEmail())
        .createdAt(DateUtils.convertToDateString(preview.getCreatedAt()))
        .prefers(preview.getPrefers())
        .componentTypes(componentTypes)
        .build();
  }

  public DesignBoardDetailDto toDesignBoardDetailDto(
      DesignBoardQuery.Detail detail,
      List<Tag> tags,
      List<String> previewImageList,
      List<BoardComponentTypeDto> componentTypes
  ) {
    return DesignBoardDetailDto.builder()
        .postId(detail.getPostId())
        .title(detail.getTitle())
        .content(detail.getContent())
        .userEmail(detail.getUserEmail())
        .userName(detail.getUserName())
        .createdAt(DateUtils.convertToDateString(detail.getCreatedAt()))
        .previewImageUrl(previewImageList)
        .prefers(detail.getPrefers())
        .comments(detail.getComments())
        .tags(tags.stream().map(TagResponse::from).toList())
        .liked(detail.isLiked())
        .bookmarked(detail.isBookmarked())
        .following(detail.isFollowing())
        .profileImage(detail.getProfileImage())
        .componentTypes(componentTypes)
        .build();
  }
}
