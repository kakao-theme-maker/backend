package com.komentum.test.data;

import com.github.javafaker.Faker;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DesignComponentDataGenerator {

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  private final Faker faker = new Faker();

  @Getter
  private List<User> users;

  @Getter
  private List<DesignComponent> designComponents;

  /**
   * 테스트용 DesignComponent 데이터 생성
   *
   * @param userCount        생성할 사용자 수
   * @param componentPerUser 사용자당 생성할 디자인 컴포넌트 수
   */
  public void generateData(int userCount, int componentPerUser) {
    this.users = userDataGenerator.generateTestUsers(userCount);
    this.designComponents = generateDesignComponents(users, componentPerUser);
  }

  /**
   * 다수 사용자에 DesignComponent 생성
   *
   * @param users            사용자 리스트
   * @param componentPerUser 사용자당 생성할 컴포넌트 수
   * @return 생성된 DesignComponent 리스트
   */
  public List<DesignComponent> generateDesignComponents(List<User> users, int componentPerUser) {
    return generateDesignComponents(users, componentPerUser, List.of());
  }

  public List<DesignComponent> generateDesignComponents(List<User> users, int componentPerUser,
      List<ComponentType> componentTypes) {
    List<DesignComponent> components = new ArrayList<>();
    for (User user : users) {
      for (int j = 0; j < componentPerUser; j++) {
        DesignComponent designComponent = DesignComponent.builder()
            .user(user)
            .imageUrl(faker.internet().image())
            .isPublic(j % 2 == 0) // 짝수일 때는 public 홀수일때는 private
            .build();
        designComponent.replaceComponentTypes(componentTypes);
        components.add(designComponent);
      }
    }
    return designComponentRepository.saveAll(components);
  }


  /**
   * 단일 DesignComponent 생성
   *
   * @param user     소유자
   * @param imageUrl 이미지 URL
   * @param isPublic 공개 여부
   * @return 생성된 DesignComponent
   */
  public DesignComponent generateDesignComponent(User user, String imageUrl, Boolean isPublic) {
    return generateDesignComponent(user, imageUrl, isPublic, List.of());
  }

  public DesignComponent generateDesignComponent(User user, String imageUrl, Boolean isPublic,
      List<ComponentType> componentTypes) {
    DesignComponent designComponent = DesignComponent.builder()
        .user(user)
        .imageUrl(imageUrl)
        .isPublic(isPublic)
        .build();
    designComponent.replaceComponentTypes(componentTypes);
    return designComponentRepository.save(designComponent);
  }

  /**
   * 기본값으로 단일 DesignComponent 생성
   *
   * @param user 소유자
   * @return 생성된 DesignComponent
   */
  public DesignComponent generateDesignComponent(User user) {
    return generateDesignComponent(user, faker.internet().image(), true, List.of());
  }


  /**
   * DesignComponent만 삭제.
   */
  public void deleteDesignComponents() {
    designComponentRepository.deleteAll();
  }

}
