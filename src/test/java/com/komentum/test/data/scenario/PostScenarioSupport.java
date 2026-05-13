package com.komentum.test.data.scenario;

import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.domain.enums.PostType;
import com.komentum.seed.seeder.BookmarkSeeder;
import com.komentum.seed.seeder.BookmarkSeeder.BookmarkSeedResult;
import com.komentum.seed.seeder.CategorySeeder;
import com.komentum.seed.seeder.PostSeeder;
import com.komentum.seed.seeder.PreferSeeder;
import com.komentum.seed.seeder.ThemeBoardSeeder;
import com.komentum.seed.seeder.ThemeBoardSeeder.ThemeBoardSeedResult;
import com.komentum.seed.seeder.UserSeeder;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 어떤 데이터를 생성하는지 테스트에 드러나도록 하기 위한 클래스
 * 적절한 seeder 호출과 잘못된 파라미터 예외 처리에 대한 책임을 갖는다
 * */
@Component
@RequiredArgsConstructor
public class PostScenarioSupport {

  private final UserSeeder userSeeder;
  private final PostSeeder postSeeder;
  private final PreferSeeder preferSeeder;
  private final CategorySeeder categorySeeder;
  private final BookmarkSeeder bookmarkSeeder;
  private final ThemeBoardSeeder themeBoardSeeder;

  public PostScenarioBuilder builder() {
    return new PostScenarioBuilder();
  }

  public PostScenarioBuilder builder(List<User> users) {
    return new PostScenarioBuilder(users);
  }

  /**
   * PostScenarioBuilder의 결과 클래스
   * */
  public record Result(
      List<User> users,
      List<Post> posts,
      List<Prefer> prefers,
      List<Category> categories,
      List<Category> bookmarks,
      List<CategoryPost> bookmarkMappings,
      List<ThemeBoard> themeBoards
  ) {

    public User getFirstUser() {
      return users.get(0);
    }
  }

  /**
   * 게시글 관련 테스트 데이터 생성 시나리오를 조합하는 클래스
   * */
  @NoArgsConstructor
  public class PostScenarioBuilder {

    private List<User> users;
    private List<Post> posts;
    private List<Prefer> prefers;
    private List<Category> categories;
    private List<Category> bookmarks;
    private List<CategoryPost> bookmarkMappings;
    private List<ThemeBoard> themeBoards;

    public PostScenarioBuilder(List<User> users) {
      this.users = users;
    }

    /**
     * userCount 수의 user Entity 생성
     * */
    @Transactional
    public PostScenarioBuilder withUsers(int userCount) {
      users = userSeeder.seedData(userCount);
      return this;
    }

    /**
     * user마다 postPerUser만큼의 "ThemeBoard 성격의 Post" 생성
     * todo : 추후 ThemeBoard 생성 로직으로 대체해야함 ( 우선순위 높음 )
     * */
    @Deprecated
    @Transactional
    public PostScenarioBuilder withThemeBoardPerUser(int postPerUser) {
      if (users.isEmpty()) {
        throw new RuntimeException("user must not be empty");
      }
      posts = postSeeder
          .seedPerUser(users, postPerUser, "https://test-data.com", PostType.THEME_BOARD);
      return this;
    }

    @Transactional
    public PostScenarioBuilder withThemeBoards(List<ThemeComponent> themeComponents) {
      if (users.isEmpty()) {
        throw new RuntimeException("user must not be empty");
      }
      ThemeBoardSeedResult result = themeBoardSeeder.seedData(themeComponents);
      this.posts = result.posts();
      this.themeBoards = result.themeBoards();
      return this;
    }

    /**
     * 게시글마다 preferPerPost 만큼의 prefer 생성
     */
    @Transactional
    public PostScenarioBuilder withPrefersPerPost(int preferPerPost) {
      return withPrefersPerPost(preferPerPost, 1.0);
    }

    /**
     * 게시글 중 일부(ratio)에 preferPerPost 만큼의 prefer 생성
     */
    @Transactional
    public PostScenarioBuilder withPrefersPerPost(int preferPerPost, double ratio) {
      if (users.isEmpty() || posts == null || posts.isEmpty()) {
        throw new RuntimeException("user or post must not be empty");
      }
      this.prefers = preferSeeder.seedPerPost(preferPerPost, users, ratio);
      return this;
    }

    /**
     * 사용자마다 categoryPerUser 만큼의 category 생성
     * */
    @Transactional
    public PostScenarioBuilder withCategoriesPerUser(int categoryPerUser) {
      if (users.isEmpty()) {
        throw new RuntimeException("user must not be empty");
      }
      this.categories = categorySeeder.seedCategoryPerUser(categoryPerUser, users);
      return this;
    }

    /**
     * category마다 postMappingsPerCategory 만큼의 게시글을 매핑한다
     * */
    @Transactional
    public PostScenarioBuilder withPostMappingsPerCategory(int postMappingPerCategory) {
      if (posts.size() < postMappingPerCategory) {
        throw new RuntimeException("total post size must bigger than post per category count");
      }
      categorySeeder.seedPostMappingsPerCategory(postMappingPerCategory, categories, posts);
      return this;
    }

    /**
     * 사용자마다 전체 게시글 중 bookmarkRatio만큼 북마크에 게시글을 추가한다
     * @param bookmarkRatio 전체 게시글 중 북마크에 추가할 비율 ( 0 ~ 1 )
     * */
    @Transactional
    public PostScenarioBuilder withBookmarkRatio(double bookmarkRatio) {
      if (bookmarkRatio < 0 || bookmarkRatio > 1) {
        throw new IllegalArgumentException("bookmarkRatio must be between 0 and 1");
      }
      BookmarkSeedResult result = bookmarkSeeder.bookmarkByRatio(users, posts, bookmarkRatio);
      bookmarks = result.bookmarks();
      bookmarkMappings = result.bookmarkMappings();
      return this;
    }

    public Result build() {
      return new Result(users, posts, prefers, categories, bookmarks, bookmarkMappings,
          themeBoards);
    }
  }
}
