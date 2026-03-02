package com.komentum.post.mapper;

import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.query.ThemeBoardQuery;
import com.komentum.post.facade.BoardManagementHelper;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ThemeBoardMapperSupport {

  /**
   * 파라미터를 기반으로 ThemeBoardPreviewDto 목록 매핑
   * @param previewList ThemeBoardPreviewDto 생성에 필요한 데이터 목록
   * @param boardManagementHelper 대표 이미지 정보 -> 대표 이미지 URL 변환을 위한 객체
   * @return ThemeBoardPreviewDto 목록 반환
   * */
  public List<ThemeBoardPreviewDto> toThemeBoardPreviewDtoList(
      List<ThemeBoardQuery.Preview> previewList,
      BoardManagementHelper boardManagementHelper) {
    return previewList.stream()
        .map(preview -> ThemeBoardPreviewDto.builder()
            .postId(preview.getPostId())
            .themeComponentId(preview.getThemeComponentId())
            .title(preview.getTitle())
            .previewImageUrl(
                boardManagementHelper.findPreviewImageUrl(preview.getPreviewImageName()))
            .userEmail(preview.getUserEmail())
            .createdAt(DateUtils.convertToDateString(preview.getCreatedAt()))
            .prefers(preview.getPrefers())
            .build())
        .toList();
  }

  /**
   * 파라미터 기반으로 ThemeBoardDetailDto 매핑
   * @param postSummary post aggregate DTO
   * @param themeBoard 게시글-테마 매핑 Entity
   * @param boardManagementHelper 대표 이미지 정보 -> 대표 이미지 URL 변환을 위한 객체
   * */
  public ThemeBoardDetailDto toThemeBoardDetailDto(PostSummary postSummary, ThemeBoard themeBoard,
      BoardManagementHelper boardManagementHelper) {
    String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
        postSummary.findPreviewImageName());
    return ThemeBoardDetailDto.from(postSummary.getPost(), themeBoard.getThemeComponent(),
        postSummary.getAuthor(), postSummary.getPrefers(), previewImageUrl);
  }
}
