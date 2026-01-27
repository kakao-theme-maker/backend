package com.komentum.test;

import com.github.javafaker.Faker;
import com.komentum.global.security.UserRole;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class UserDataGenerator {
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  UserRepository userRepository;

  Faker faker = new Faker();

    public UserDataGenerator(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void generateTestUser(String userEmail) {
    String uuid = UUID.randomUUID().toString(); //publicUserId 생성
    userRepository.save(User.builder()
        .publicUserId(uuid)
        .profileImg(faker.internet().image())
        .birth(LocalDate.now())
        .gender(Gender.male)
        .role(UserRole.USER)
        .userEmail(userEmail)
        .build());
  }


  public void generateTestLocalUser(String userEmail, String password) {
    String uuid = UUID.randomUUID().toString();
    userRepository.save(User.builder()
            .publicUserId(uuid)
            .profileImg(faker.internet().image())
            .birth(LocalDate.now())
            .gender(Gender.male)
            .role(UserRole.USER)
            .userEmail(userEmail)
            .encryptedPassword(bCryptPasswordEncoder.encode(password))
            .build());
  }

  public void generateRetrieveTestUser(String userEmail, String password) {
    String uuid = UUID.randomUUID().toString();
    userRepository.save(User.builder()
            .publicUserId(uuid)
        .profileImg("https://example")
        .birth(LocalDate.now())
        .gender(Gender.male)
        .role(UserRole.USER)
        .userEmail("admin1@gmail.com")
        .name("admin")
        .encryptedPassword(bCryptPasswordEncoder.encode(password))
        .build());
  }

  public void deleteAllUsers() {
    userRepository.deleteAll();
  }
}
