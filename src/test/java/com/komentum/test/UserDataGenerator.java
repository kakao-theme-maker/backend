package com.komentum.test;

import com.github.javafaker.Faker;
import com.komentum.global.security.UserRole;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDataGenerator {

  @Autowired
  UserRepository userRepository;

  Faker faker = new Faker();

  public void generateTestUser(String userEmail) {
    userRepository.save(User.builder()
        .profileImg(faker.internet().image())
        .birth(LocalDate.now())
        .gender(Gender.male)
        .role(UserRole.USER)
        .userEmail(userEmail)
        .build());
  }

  public void deleteAllUsers() {
    userRepository.deleteAll();
  }
}
