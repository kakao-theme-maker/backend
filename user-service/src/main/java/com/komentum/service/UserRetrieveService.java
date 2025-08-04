package com.komentum.service;

import com.komentum.domain.User;
import com.komentum.dto.UserResponseDto;
import com.komentum.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserRetrieveService {

  private final UserRepository userRepository;

  public UserRetrieveService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponseDto getUserByEmail(String email) {
    User user = userRepository.findById(email)
        .orElseThrow(() -> new RuntimeException("user not found"));
    return UserResponseDto.from(user);
  }
}
