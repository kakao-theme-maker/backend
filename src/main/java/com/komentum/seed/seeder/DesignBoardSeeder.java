package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DesignBoardSeeder {

  private final DesignBoardRepository designBoardRepository;
  private final PostSeeder postSeeder;
  private final UserRepository userRepository;
  private final Faker faker;
  private final FileManager fileManager;

  private DesignBoard generateOne(DesignComponent designComponent, Post post) {
    return DesignBoard.builder()
        .designComponent(designComponent)
        .post(post)
        .build();
  }

  @Transactional
  public List<DesignBoard> seedData(List<DesignComponent> designComponents) {
    List<DesignBoard> designBoards = new ArrayList<>();
    int size = designComponents.size();
    for (int i = 0; i < size; i++) {
      DesignComponent component = designComponents.get(i);
      User author = userRepository.findByUserEmail(component.getUserEmail())
          .orElseThrow(() -> new IllegalArgumentException(
              "user with " + component.getUserEmail() + " doesn't exists"));
      Post post = postSeeder.createOne(author,
          fileManager.convertUrlToFileName(component.getImageUrl()));
      designBoards.add(generateOne(component, post));
    }
    return designBoardRepository.saveAll(designBoards);
  }
}
