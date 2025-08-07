package com.komentum.user.service;

import com.komentum.user.domain.User;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.UserRepository;
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
