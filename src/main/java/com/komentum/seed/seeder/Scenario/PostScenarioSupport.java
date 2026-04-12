package com.komentum.seed.seeder.Scenario;

import com.komentum.post.domain.Category;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.seed.seeder.CategorySeeder;
import com.komentum.seed.seeder.PostSeeder;
import com.komentum.seed.seeder.PreferSeeder;
import com.komentum.seed.seeder.UserSeeder;
import com.komentum.user.domain.User;
import java.util.List;
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

  public PostScenarioBuilder builder() {
    return new PostScenarioBuilder();
  }

  /**
   * PostScenarioBuilder의 결과 클래스
   * */
  public record Result(
      List<User> users,
      List<Post> posts,
      List<Prefer> prefers,
      List<Category> categories
  ) {

    public User getFirstUser() {
      return users.get(0);
    }
  }

  /**
   * 게시글 관련 테스트 데이터 생성 시나리오를 조합하는 클래스
   * */
  public class PostScenarioBuilder {

    private List<User> users;
    private List<Post> posts;
    private List<Prefer> prefers;
    private List<Category> categories;

    /**
     * userCount 수의 user Entity 생성
     * */
    @Transactional
    public PostScenarioBuilder withUsers(int userCount) {
      users = userSeeder.seedData(userCount);
      return this;
    }

    /**
     * user마다 postPerUser만큼의 post 생성
     * */
    @Transactional
    public PostScenarioBuilder withPostPerUser(int postPerUser) {
      if (users.isEmpty()) {
        throw new RuntimeException("user must not be empty");
      }
      posts = postSeeder.seedPerUser(users, postPerUser, "https://test-data.com");
      return this;
    }

    /**
     * 게시글마다 preferPerPost 만큼의 prefer 생성
     */
    @Transactional
    public PostScenarioBuilder withPrefersPerPost(int preferPerPost) {
      if (users.isEmpty() || posts.isEmpty()) {
        throw new RuntimeException("user or post must not be empty");
      }
      this.prefers = preferSeeder.seedPerPost(preferPerPost, users);
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

    public Result build() {
      return new Result(users, posts, prefers, categories);
    }
  }
}
