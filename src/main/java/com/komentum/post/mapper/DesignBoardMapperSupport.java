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

  /**
   * 활성 에셋의 타입만 모아 TypeCode 기준으로 중복을 제거한다.
   * 입력 연결과 타입을 순회하며 처음 발견한 순서를 유지한다.
   */
  public List<BoardComponentTypeDto> toComponentTypeDtos(List<DesignBoard> designBoards) {
    Map<TypeCode, BoardComponentTypeDto> componentTypeMap = new LinkedHashMap<>();
    for (DesignBoard designBoard : designBoards) {
      if (!Boolean.TRUE.equals(designBoard.getDesignComponent().getIsActive())) {
        continue;
      }
      for (ComponentType componentType : designBoard.getDesignComponent().getComponentTypes()) {
        componentTypeMap.putIfAbsent(componentType.getTypeCode(),
            BoardComponentTypeDto.from(componentType));
      }
    }
    return List.copyOf(componentTypeMap.values());
  }

  /**
   * 활성 연결의 미리보기 조회 결과에 노출 정책을 적용한 대표 이미지와 타입을 결합한다.
   * 대표 이미지 선택과 타입 필터링은 호출 측에서 완료한 값을 사용한다.
   */
  public DesignBoardPreviewDto toDesignBoardPreviewDto(
      DesignBoardQuery.Preview preview,
      BoardManagementHelper helper,
      List<BoardComponentTypeDto> componentTypes,
      String previewImageName
  ) {
    return DesignBoardPreviewDto.builder()
        .postId(preview.getPostId())
        .designComponentId(preview.getDesignComponentId())
        .title(preview.getTitle())
        .previewImageUrl(helper.findPreviewImageUrl(previewImageName))
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
