package com.komentum.test;

import com.komentum.config.PostTestDataGenerator;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThemeBoardDataGenerator {

  @Autowired
  private ThemeBoardRepository themeBoardRepository;

  @Autowired
  private PostTestDataGenerator postTestDataGenerator;

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  private final List<ThemeBoard> themeBoards = new ArrayList<>();
  private final List<ThemeComponent> nonThemeBoardThemeComponents = new ArrayList<>();

  public void generateThemeBoards(int userCount, int postPerUsers, int commentPerPosts) {
    postTestDataGenerator.generateData(userCount, postPerUsers, commentPerPosts);
    themeDataGenerator.generateTestData(getPosts().size());
    List<Post> allPosts = getPosts();
    List<ThemeComponent> allThemeComponents = getThemeComponents();
    for (int i = 0; i < allThemeComponents.size() / 2; i++) {
      Post post = allPosts.get(i);
      ThemeComponent theme = getThemeComponents().get(i);
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

  public List<Post> getPosts() {
    return postTestDataGenerator.posts;
  }

  public List<ThemeComponent> getThemeComponents() {
    return themeDataGenerator.initialThemes;
  }

  public List<User> getUsers() {
    return postTestDataGenerator.users;
  }

  public List<ThemeBoard> getThemeBoards() {
    return themeBoards;
  }

  public List<ThemeComponent> getNonThemeBoardThemeComponents() {
    return nonThemeBoardThemeComponents;
  }
}
