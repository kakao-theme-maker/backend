package com.komentum.test.data;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BoardDetailDataGenerator {

  @Autowired
  private ThemeBoardRepository themeBoardRepository;

  @Autowired
  private DesignBoardRepository designBoardRepository;

  @Autowired
  private PostTestDataGenerator postTestDataGenerator;

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  // design board data
  private final List<DesignBoard> designBoards = new ArrayList<>();
  private final List<DesignComponent> nonDesignBoardDesignComponents = new ArrayList<>();

  // theme board data
  private final List<ThemeBoard> themeBoards = new ArrayList<>();
  private final List<ThemeComponent> nonThemeBoardThemeComponents = new ArrayList<>();

  // theme board generator
  public void generateThemeBoards(int userCount, int postPerUsers, int commentPerPosts,
      int maxPreferPerPost) {
    postTestDataGenerator.generateDataWithPrefer(userCount, postPerUsers, commentPerPosts,
        maxPreferPerPost, PostType.THEME_BOARD);
    themeDataGenerator.generateTestData(getPosts().size());
    List<Post> allPosts = getPosts();
    List<ThemeComponent> allThemeComponents = themeDataGenerator.initialThemes;
    for (int i = 0; i < allThemeComponents.size() / 2; i++) {
      Post post = allPosts.get(i);
      ThemeComponent theme = allThemeComponents.get(i);
      ThemeBoard themeBoard = themeBoardRepository.save(
          ThemeBoard.builder().post(post).themeComponent(theme).build()
      );
      themeBoards.add(themeBoard);
    }
    nonThemeBoardThemeComponents.addAll(
        allThemeComponents.subList(allThemeComponents.size() / 2, allThemeComponents.size()));
  }

  public void deleteThemeBoards() {
    themeBoardRepository.deleteAll();
    themeDataGenerator.deleteTestData();
    postTestDataGenerator.deleteData();
    themeBoards.clear();
    nonThemeBoardThemeComponents.clear();
  }

  // design board generator
  public void generateDesignBoards(int userCount, int postPerUsers, int commentPerPosts) {
    postTestDataGenerator.generateData(userCount, postPerUsers, commentPerPosts,
        PostType.DESIGN_BOARD);
    themeDataGenerator.generateTestData(getPosts().size());
    List<Post> allPosts = getPosts();
    List<DesignComponent> allDesignComponents = themeDataGenerator.initialDesignComponents;
    for (int i = 0; i < allPosts.size() / 2; i++) {
      DesignBoard designBoard = designBoardRepository.save(DesignBoard.builder()
          .designComponent(allDesignComponents.get(i))
          .post(allPosts.get(i))
          .build());
      designBoards.add(designBoard);
    }
    nonDesignBoardDesignComponents.addAll(
        allDesignComponents.subList(allDesignComponents.size() / 2, allDesignComponents.size()));
  }

  public void deleteDesignBoards() {
    designBoardRepository.deleteAll();
    themeDataGenerator.deleteTestData();
    postTestDataGenerator.deleteData();
    designBoards.clear();
    nonDesignBoardDesignComponents.clear();
  }

  // common
  public List<Post> getPosts() {
    return postTestDataGenerator.posts;
  }

  public List<User> getUsers() {
    return postTestDataGenerator.users;
  }
}
