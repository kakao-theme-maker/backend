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
      List<PostQuery.Detail> details) {
    List<Long> posts = details.stream().map(p -> p.getPost().getPostId()).toList();
    Map<Long, List<Tag>> tags = tagService.getTagPerPosts(posts);
    return details.stream().map(detail -> {
      List<Tag> tagList = tags.getOrDefault(detail.getPost().getPostId(), List.of());
      return UserPostListResponseDto.from(detail, tagList, helper);
    }).toList();
  }
}
