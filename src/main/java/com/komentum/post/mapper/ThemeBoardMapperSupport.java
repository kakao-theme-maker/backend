package com.komentum.post.mapper;

import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.facade.BoardManagementHelper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ThemeBoardMapperSupport {

  public List<ThemeBoardPreviewDto> toThemeBoardPreviewDtoList(
      List<PostSummary> postSummaries,
      Map<Long, ThemeBoard> postThemeBoardMap,
      BoardManagementHelper helper) {
    return postSummaries.stream().map(postSummary -> {
          ThemeBoard themeBoard = postThemeBoardMap.get(postSummary.findPostId());
          String previewImageUrl = helper.findPreviewImageUrl(
              postSummary.findPreviewImageName());
          return ThemeBoardPreviewDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
              postSummary.getAuthor(),
              postSummary.getPrefers(), previewImageUrl);
        })
        .toList();
  }

  public ThemeBoardDetailDto toThemeBoardDetailDto(PostSummary postSummary, ThemeBoard themeBoard,
      BoardManagementHelper boardManagementHelper) {
    String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
        postSummary.findPreviewImageName());
    return ThemeBoardDetailDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
        postSummary.getAuthor(), postSummary.getPrefers(), previewImageUrl);
  }
}
