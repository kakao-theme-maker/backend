package com.komentum.config;

import com.github.javafaker.Faker;
import com.komentum.domain.Gender;
import com.komentum.domain.User;
import com.komentum.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserTestDataGenerator {

  private final UserRepository userRepository;

  public UserTestDataGenerator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<String> userEmails;

  public void generateFakeData() {
    Faker faker = new Faker();
    userEmails = generateUsers(faker, 20);
  }

  public void removeFakeData() {
    userRepository.deleteAll();
    userEmails = new ArrayList<>();
  }

  public List<String> generateUsers(Faker faker, int count) {
    List<String> userEmails = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      User savedUser = userRepository.save(User.builder()
          .userEmail(UUID.randomUUID() + "@gmail.com")
          .profileImg(faker.avatar().image())
          .gender(Gender.male)
          .birth(LocalDate.now())
          .introduce(faker.lorem().word())
          .build());
      userEmails.add(savedUser.getUserEmail());
    }
    return userEmails;
  }
}
