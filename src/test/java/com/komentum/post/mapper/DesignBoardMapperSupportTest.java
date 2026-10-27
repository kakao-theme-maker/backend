package com.komentum.post.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.dto.BoardComponentTypeDto;
import com.komentum.post.dto.DesignBoardDto.DesignComponentMappingDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DesignBoardMapperSupportTest {

  private final DesignBoardMapperSupport mapper = new DesignBoardMapperSupport();

  @Test
  @DisplayName("여러 에셋이 같은 타입을 사용해도 게시글 타입 목록에는 한 번만 반환한다")
  void toComponentTypeDtos_deduplicatesBoardTypes() {
    ComponentType first = componentType(10, TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE);
    ComponentType second = componentType(20, TypeCode.CHAT_ROOM_BACKGROUND_IMAGE);

    List<BoardComponentTypeDto> result = mapper.toComponentTypeDtos(List.of(
        board(2, second), board(1, first), board(2, first)));

    assertThat(result).extracting(BoardComponentTypeDto::getComponentTypeId)
        .containsExactlyInAnyOrder(10, 20);
    assertThat(result).extracting(BoardComponentTypeDto::getTypeCode)
        .containsExactlyInAnyOrder(first.getTypeCode(), second.getTypeCode());
  }

  @Test
  @DisplayName("게시글 연결행을 에셋별 타입 ID 목록으로 묶어 반환한다")
  void toDesignComponentMappings_groupsAssets() {
    ComponentType first = componentType(10, TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE);
    ComponentType second = componentType(20, TypeCode.CHAT_ROOM_BACKGROUND_IMAGE);

    List<DesignComponentMappingDto> result = mapper.toDesignComponentMappings(List.of(
        board(2, second), board(1, second), board(1, first)));

    assertThat(result).usingRecursiveComparison().ignoringCollectionOrder()
        .isEqualTo(List.of(new DesignComponentMappingDto(1, List.of(10, 20)),
            new DesignComponentMappingDto(2, List.of(20))));
  }

  @Test
  @DisplayName("게시글 연결이 없으면 타입과 에셋 매핑 모두 빈 목록을 반환한다")
  void emptyBoards_returnEmptyLists() {
    assertThat(mapper.toComponentTypeDtos(List.of())).isEmpty();
    assertThat(mapper.toDesignComponentMappings(List.of())).isEmpty();
  }

  /**
   * 게시글에 연결할 테스트 타입을 생성한다.
   */
  private ComponentType componentType(int id, TypeCode typeCode) {
    return ComponentType.builder()
        .componentTypeId(id)
        .typeCode(typeCode)
        .build();
  }

  /**
   * 에셋 ID와 타입만 가진 게시글 연결행을 생성한다.
   */
  private DesignBoard board(int designComponentId, ComponentType componentType) {
    return DesignBoard.builder()
        .designComponent(DesignComponent.builder().designComponentId(designComponentId).build())
        .componentType(componentType)
        .build();
  }
}
