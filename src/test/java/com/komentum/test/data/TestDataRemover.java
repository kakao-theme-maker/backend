package com.komentum.test.data;

import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.post.repository.CommentRepository;
import com.komentum.post.repository.DesignBoardRepository;
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
  private final DesignBoardRepository designBoardRepository;

  /**
   * 모든 테스트 코드에서 사용할 수 있는 데이터베이스 clean용 클래스
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
    designBoardRepository.deleteAll();
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
