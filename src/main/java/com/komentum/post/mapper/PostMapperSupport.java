package com.komentum.post.mapper;

import com.komentum.post.domain.Tag;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.dto.query.PostQuery;
import com.komentum.post.facade.BoardManagementHelper;
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

  @Transactional(readOnly = true)
  public List<UserPostListResponseDto> toUserPostListResponseDtoList(
      List<PostQuery.UserPostListRow> rows) {
    if (rows.isEmpty()) {
      return List.of();
    }
    List<Long> postIds = rows.stream().map(PostQuery.UserPostListRow::getPostId).toList();
    Map<Long, List<Tag>> tagMap = tagService.getTagPerPosts(postIds);
    return rows.stream().map(row -> {
      Long postId = row.getPostId();
      List<Tag> tagList = tagMap.getOrDefault(postId, List.of());
      return UserPostListResponseDto.from(row, tagList, helper);
    }).toList();
  }
}
