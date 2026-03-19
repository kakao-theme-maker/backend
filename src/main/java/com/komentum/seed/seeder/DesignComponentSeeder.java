package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DesignComponentSeeder {

  private final DesignComponentRepository designComponentRepository;
  private final Faker faker;

  private DesignComponent generateOne(User owner) {
    return DesignComponent.builder()
        .userEmail(owner.getUserEmail())
        .imageUrl("https://persistent.oaistatic.com/images-app/clouds.webp")
        .isPublic(true)
        .build();
  }

  @Transactional
  public List<DesignComponent> seedPeruser(int size, List<User> owners) {
    List<String> userEmails = owners.stream().map(User::getUserEmail).toList();
    List<DesignComponent> existing = designComponentRepository.findByUserEmailIn(
        userEmails);
    if (existing.size() >= size * owners.size()) {
      return existing;
    }
    List<DesignComponent> designComponents = new ArrayList<>();
    for (User owner : owners) {
      for (int i = 0; i < size; i++) {
        designComponents.add(generateOne(owner));
      }
    }
    return designComponentRepository.saveAll(designComponents);
  }

}
