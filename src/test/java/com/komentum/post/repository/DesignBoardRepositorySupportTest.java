package com.komentum.post.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.security.UserRole;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DesignBoardRepositorySupportTest {

  @Autowired
  private TestEntityManager entityManager;

  private DesignBoardRepositorySupport repository;
  private User author;
  private ComponentType firstType;
  private ComponentType secondType;

  /**
   * 실제 JPA 쿼리를 실행할 저장소와 테스트에 필요한 작성자·타입을 준비한다.
   */
  @BeforeEach
  void setUp() {
    JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager.getEntityManager());
    PostRepositorySupport postRepository = new PostRepositorySupport(queryFactory,
        mock(PreferRepository.class), mock(PreferRepositorySupport.class));
    repository = new DesignBoardRepositorySupport(queryFactory, postRepository);
    author = user("author");
    firstType = componentType(TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE);
    secondType = componentType(TypeCode.MESSAGE_CELL_STYLE_RECEIVE_BACKGROUND_IMAGE);
  }

  /**
   * 복수 에셋·타입 매핑에서도 게시글 단위 페이지와 정확한 좋아요 수를 반환하는지 검증한다.
   */
  @Test
  @DisplayName("여러 에셋·타입 연결이 있어도 게시글 단위로 페이지를 나누고 좋아요 수를 중복 집계하지 않는다")
  void findLists_paginatePostsWithoutDuplicatingPrefers() {
    DesignComponent firstAsset = asset("first.png");
    DesignComponent secondAsset = asset("second.png");
    Post newest = post("newest", 3);
    Post middle = post("middle", 2);
    Post oldest = post("oldest", 1);
    board(newest, secondAsset, firstType);
    board(newest, firstAsset, firstType);
    board(newest, firstAsset, secondType);
    board(middle, secondAsset, secondType);
    board(oldest, firstAsset, firstType);
    entityManager.persist(Prefer.createTransient(newest, user("first-liker")));
    entityManager.persist(Prefer.createTransient(newest, user("second-liker")));
    entityManager.flush();

    List<DesignBoardQuery.Preview> firstPage = repository.findPreviewList(PageRequest.of(0, 2));
    List<DesignBoardQuery.Preview> secondPage = repository.findPreviewList(PageRequest.of(1, 2));

    assertThat(firstPage).extracting(DesignBoardQuery.Preview::getPostId)
        .containsExactly(newest.getPostId(), middle.getPostId());
    assertThat(secondPage).extracting(DesignBoardQuery.Preview::getPostId)
        .containsExactly(oldest.getPostId());
    assertThat(firstPage.get(0).getPrefers()).isEqualTo(2);
    assertThat(repository.findDetailByPostId(newest.getPostId(), null).getPrefers())
        .isEqualTo(2);
    assertThat(repository.findDesignBoardDetails(PageRequest.of(0, 2), null,
        new PostSearchCondition(), List.of(PostSortType.CREATED_DESC)))
        .extracting(DesignBoardQuery.Detail::getPostId)
        .containsExactly(newest.getPostId(), middle.getPostId());
    assertThat(repository.findDesignBoardDetails(PageRequest.of(1, 2), null,
        new PostSearchCondition(), List.of(PostSortType.CREATED_DESC)))
        .extracting(DesignBoardQuery.Detail::getPostId)
        .containsExactly(oldest.getPostId());
  }

  @Test
  @DisplayName("공유 에셋의 다른 게시글 타입과 무관하게 게시글 타입·검색어 일치를 우선 정렬한다")
  void findLists_prioritizeBoardTypeAndKeywordWithoutFilteringOtherPosts() {
    DesignComponent sharedAsset = asset("shared.png");
    Post matching = post("target matching", 1);
    Post wrongType = post("target other type", 2);
    Post wrongKeyword = post("other keyword", 3);
    board(matching, sharedAsset, firstType);
    board(wrongType, sharedAsset, secondType);
    board(wrongKeyword, sharedAsset, firstType);
    entityManager.flush();
    PostSearchCondition condition = new PostSearchCondition()
        .withKeyword("target")
        .withTypeCode(firstType.getTypeCode());

    assertThat(repository.findPreviewList(PageRequest.of(0, 10), condition,
        List.of(PostSortType.CREATED_DESC)))
        .extracting(DesignBoardQuery.Preview::getPostId)
        .containsExactly(matching.getPostId(), wrongType.getPostId(), wrongKeyword.getPostId());
    assertThat(repository.findDesignBoardDetails(PageRequest.of(0, 10), null, condition,
        List.of(PostSortType.CREATED_DESC)))
        .extracting(DesignBoardQuery.Detail::getPostId)
        .containsExactly(matching.getPostId(), wrongType.getPostId(), wrongKeyword.getPostId());
  }

  /**
   * 유일한 이메일과 공개 ID를 가진 테스트 사용자를 저장한다.
   */
  private User user(String name) {
    return entityManager.persist(User.builder()
        .publicUserId(name)
        .userEmail(name + "@example.com")
        .name(name)
        .role(UserRole.USER)
        .build());
  }

  /**
   * 정렬과 타입 검색에 사용할 컴포넌트 타입을 저장한다.
   */
  private ComponentType componentType(TypeCode typeCode) {
    return entityManager.persist(ComponentType.builder()
        .typeCode(typeCode)
        .name(typeCode.name())
        .build());
  }

  /**
   * 타입을 갖지 않는 디자인 에셋을 저장한다.
   */
  private DesignComponent asset(String fileName) {
    return entityManager.persist(DesignComponent.builder()
        .user(author)
        .fileName(fileName)
        .isPublic(true)
        .build());
  }

  /**
   * 게시글 생성 시각을 지정해 목록 정렬을 결정적으로 검증한다.
   */
  private Post post(String title, int day) {
    Post post = entityManager.persist(Post.builder()
        .user(author)
        .title(title)
        .content("content")
        .postType(PostType.DESIGN_BOARD)
        .build());
    post.setCreatedAt(LocalDateTime.of(2026, 1, day, 12, 0));
    return post;
  }

  /**
   * 게시글·에셋·타입의 개별 연결행을 저장한다.
   */
  private void board(Post post, DesignComponent asset, ComponentType componentType) {
    entityManager.persist(DesignBoard.builder()
        .post(post)
        .designComponent(asset)
        .componentType(componentType)
        .build());
  }
}
