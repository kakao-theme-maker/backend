package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.global.security.UserRole;
import com.komentum.global.utils.DateUtils;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserSeeder {

  private final UserRepository userRepository;
  private final Faker faker;
  private final BCryptPasswordEncoder passwordEncoder;

  private User generateOne() {
    return User.builder()
        .userEmail(UUID.randomUUID() + "@test.com")
        .publicUserId(UUID.randomUUID().toString())
        .encryptedPassword(passwordEncoder.encode("1234"))
        .role(UserRole.USER)
        .birth(DateUtils.toLocalDate(faker.date().birthday()))
        .gender(
            faker.number().randomDigit() % 2 == 0 ?
                Gender.male :
                Gender.female)
        .profileImg(faker.internet().image())
        .introduce(faker.lorem().word())
        .build();
  }

  @Transactional
  public List<User> seedData(int size) {
    int current = (int) userRepository.count();
    if (current >= size) {
      return userRepository.findAll(Pageable.ofSize(size)).getContent();
    }
    List<User> users = new ArrayList<>();
    int remaining = size - current;
    for (int i = 0; i < remaining; i++) {
      users.add(generateOne());
    }
    userRepository.saveAll(users);
    return userRepository.findAll(Pageable.ofSize(size)).getContent();
  }
}
