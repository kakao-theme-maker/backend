package com.komentum.seed.seeder;

import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
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
  private final FileManager fileManager;

  public record DesignBoardSeedResult(
      List<DesignBoard> designBoards,
      List<Post> posts
  ) {

  }

  private DesignBoard generateOne(DesignComponent designComponent, Post post) {
    return DesignBoard.builder()
        .designComponent(designComponent)
        .post(post)
        .build();
  }

  @Transactional
  public DesignBoardSeedResult seedData(List<DesignComponent> designComponents) {
    List<DesignBoard> designBoards = new ArrayList<>();
    List<Post> posts = new ArrayList<>();
    for (DesignComponent component : designComponents) {
      DesignBoardSeedResult singlePostResult = seedWithSinglePost(List.of(component));
      designBoards.addAll(singlePostResult.designBoards());
      posts.addAll(singlePostResult.posts());
    }
    return new DesignBoardSeedResult(designBoards, posts);
  }

  @Transactional
  public DesignBoardSeedResult seedWithSinglePost(List<DesignComponent> designComponents) {
    if (designComponents.isEmpty()) {
      throw new IllegalArgumentException("designComponent list must not be empty");
    }
    DesignComponent component = designComponents.get(0);
    User author = component.getUser();
    return seedWithSinglePost(author, designComponents);
  }

  @Transactional
  public DesignBoardSeedResult seedWithSinglePost(User author,
      List<DesignComponent> designComponents) {
    if (designComponents.isEmpty()) {
      throw new IllegalArgumentException("designComponent list must not be empty");
    }
    List<DesignBoard> designBoards = new ArrayList<>();
    DesignComponent component = designComponents.get(0);
    Post targetPost = postSeeder.createOne(author,
        fileManager.convertUrlToFileName(component.getImageUrl()), PostType.DESIGN_BOARD);
    for (DesignComponent designComponent : designComponents) {
      designBoards.add(generateOne(designComponent, targetPost));
    }
    List<DesignBoard> savedDesignBoards = designBoardRepository.saveAll(designBoards);
    return new DesignBoardSeedResult(savedDesignBoards, List.of(targetPost));
  }
}
