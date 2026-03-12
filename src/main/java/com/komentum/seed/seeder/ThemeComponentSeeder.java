package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ThemeComponentSeeder {

  private final ThemeComponentRepository themeComponentRepository;
  private final Faker faker;

  private ThemeComponent generateOne(User author) {
    return ThemeComponent.builder()
        .themeName(faker.lorem().word())
        .versionName("1.1.1.1")
        .versionNumber("0")
        .isDone(true)
        .isPublic(true)
        .userEmail(author.getUserEmail())
        .build();
  }

  @Transactional
  public List<ThemeComponent> seedPerUser(int size, List<User> authors) {
    List<String> userEmailList = authors.stream()
        .map(User::getUserEmail)
        .toList();
    List<ThemeComponent> existing = themeComponentRepository.findByUserEmailIn(userEmailList);
    if (existing.size() > size * authors.size()) {
      return existing;
    }
    List<ThemeComponent> themeComponents = new ArrayList<>();
    for (User author : authors) {
      for (int i = 0; i < size; i++) {
        themeComponents.add(generateOne(author));
      }
    }
    return themeComponentRepository.saveAll(themeComponents);
  }
}
