package com.komentum.test.data;

import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PreferRepository;
import com.komentum.post.repository.TagRepository;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import com.komentum.theme.theme.repository.ThemeStyleRepository;
import com.komentum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TestDataRemover {

  private final UserRepository userRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ColorStyleRepository colorStyleRepository;
  private final DesignComponentRepository designComponentRepository;
  private final ThemeImageRepository themeImageRepository;
  private final ThemeStyleRepository themeStyleRepository;
  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeBoardRepository themeBoardRepository;
  private final CommentRepository commentRepository;
  private final PreferRepository preferRepository;
  private final TagRepository tagRepository;
  private final PostRepository postRepository;
  private final CategoryPostRepository categoryPostRepository;
  private final CategoryRepository categoryRepository;

  /**
   * 반드시 테스트용 DB를 H2 MySQL 버전으로 사용할 때만 호출해야함
   * */
  @Transactional
  public void deleteAll() {
    // delete prefer, comment, tags
    commentRepository.deleteAll();
    preferRepository.deleteAll();
    tagRepository.deleteAll();
    // delete category
    categoryPostRepository.deleteAll();
    categoryRepository.deleteAll();
    // delete theme and design boards
    themeBoardRepository.deleteAll();
    designComponentRepository.deleteAll();
    postRepository.deleteAll();
    // delete theme components
    themeImageRepository.deleteAll();
    themeStyleRepository.deleteAll();
    themeComponentRepository.deleteAll();
    // delete design components
    designComponentRepository.deleteAll();
    // delete component type and color style
    componentTypeRepository.deleteAll();
    colorStyleRepository.deleteAll();
    // delete user
    userRepository.deleteAll();
  }
}
