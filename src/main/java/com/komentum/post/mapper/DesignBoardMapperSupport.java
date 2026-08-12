package com.komentum.post.mapper;

import com.komentum.global.utils.DateUtils;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.BoardComponentTypeDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.facade.BoardManagementHelper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DesignBoardMapperSupport {

  public List<BoardComponentTypeDto> toComponentTypeDtos(List<DesignBoard> designBoards) {
    Map<TypeCode, BoardComponentTypeDto> componentTypeMap = new LinkedHashMap<>();
    for (DesignBoard designBoard : designBoards) {
      for (ComponentType componentType : designBoard.getDesignComponent().getComponentTypes()) {
        componentTypeMap.putIfAbsent(componentType.getTypeCode(),
            BoardComponentTypeDto.from(componentType));
      }
    }
    return List.copyOf(componentTypeMap.values());
  }

  public DesignBoardPreviewDto toDesignBoardPreviewDto(
      DesignBoardQuery.Preview preview,
      BoardManagementHelper helper,
      List<BoardComponentTypeDto> componentTypes
  ) {
    return DesignBoardPreviewDto.builder()
        .postId(preview.getPostId())
        .designComponentId(preview.getDesignComponentId())
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
        .profileImage(detail.getProfileImage())
        .componentTypes(componentTypes)
        .build();
  }
}
