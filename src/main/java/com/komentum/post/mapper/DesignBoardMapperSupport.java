package com.komentum.post.mapper;

import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.facade.BoardManagementHelper;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DesignBoardMapperSupport {

  public DesignBoardPreviewDto toDesignBoardPreviewDto(
      DesignBoardQuery.Preview preview,
      BoardManagementHelper helper
  ) {
    return DesignBoardPreviewDto.builder()
        .postId(preview.getPostId())
        .designComponentId(preview.getDesignComponentId())
        .title(preview.getTitle())
        .previewImageUrl(helper.findPreviewImageUrl(preview.getPreviewImageName()))
        .userEmail(preview.getUserEmail())
        .createdAt(DateUtils.convertToDateString(preview.getCreatedAt()))
        .prefers(preview.getPrefers())
        .build();
  }

  public DesignBoardDetailDto toDesignBoardDetailDto(
      DesignBoardQuery.Detail detail,
      BoardManagementHelper helper,
      List<Tag> tags
  ) {
    return DesignBoardDetailDto.builder()
        .postId(detail.getPostId())
        .title(detail.getTitle())
        .content(detail.getContent())
        .designComponentId(detail.getDesignComponentId())
        .userEmail(detail.getUserEmail())
        .createdAt(DateUtils.convertToDateString(detail.getCreatedAt()))
        .previewImageUrl(
            helper.findPreviewImageUrl(detail.getPreviewImageName())
        )
        .prefers(detail.getPrefers())
        .comments(detail.getComments())
        .tags(tags.stream().map(TagResponse::from).toList())
        .liked(detail.isLiked())
        .bookmarked(detail.isBookmarked())
        .build();
  }
}
