package com.komentum.seed.seeder;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.designcomponent.domain.DesignComponent;
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
  private final ComponentTypeRepository componentTypeRepository;
  private final PostSeeder postSeeder;

  public record DesignBoardSeedResult(
      List<DesignBoard> designBoards,
      List<Post> posts
  ) {

  }

  private DesignBoard generateOne(DesignComponent designComponent, Post post,
      ComponentType componentType) {
    return DesignBoard.builder()
        .designComponent(designComponent)
        .post(post)
        .componentType(componentType)
        .build();
  }

  @Transactional
  public DesignBoardSeedResult seedData(List<DesignComponent> designComponents) {
    List<DesignBoard> designBoards = new ArrayList<>();
    List<Post> posts = new ArrayList<>();
    List<ComponentType> componentTypes = componentTypeRepository.findAll();
    if (componentTypes.isEmpty()) {
      throw new IllegalStateException("component types must be seeded before design boards");
    }
    int typeIndex = 0;
    for (DesignComponent component : designComponents) {
      DesignBoardSeedResult singlePostResult = seedWithSinglePost(component.getUser(),
          List.of(component), List.of(componentTypes.get(typeIndex++ % componentTypes.size())));
      designBoards.addAll(singlePostResult.designBoards());
      posts.addAll(singlePostResult.posts());
    }
    return new DesignBoardSeedResult(designBoards, posts);
  }

  @Transactional
  public DesignBoardSeedResult seedWithSinglePost(User author,
      List<DesignComponent> designComponents) {
    List<ComponentType> componentTypes = componentTypeRepository.findAll();
    if (componentTypes.isEmpty()) {
      throw new IllegalStateException("component types must be seeded before design boards");
    }
    return seedWithSinglePost(author, designComponents, List.of(componentTypes.get(0)));
  }

  /**
   * 각 에셋을 지정된 모든 타입에 매핑한 디자인 게시글을 생성한다.
   */
  @Transactional
  public DesignBoardSeedResult seedWithSinglePost(User author,
      List<DesignComponent> designComponents, List<ComponentType> componentTypes) {
    if (designComponents.isEmpty()) {
      throw new IllegalArgumentException("designComponent list must not be empty");
    }
    if (componentTypes.isEmpty()) {
      throw new IllegalArgumentException("componentType list must not be empty");
    }
    List<DesignBoard> designBoards = new ArrayList<>();
    DesignComponent component = designComponents.get(0);
    Post targetPost = postSeeder.createOne(author, component.getFileName(),
        PostType.DESIGN_BOARD);
    for (DesignComponent designComponent : designComponents) {
      for (ComponentType componentType : componentTypes) {
        designBoards.add(generateOne(designComponent, targetPost, componentType));
      }
    }
    List<DesignBoard> savedDesignBoards = designBoardRepository.saveAll(designBoards);
    return new DesignBoardSeedResult(savedDesignBoards, List.of(targetPost));
  }
}
