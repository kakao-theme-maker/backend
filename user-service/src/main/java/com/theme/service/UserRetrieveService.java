package com.theme.service;

import com.theme.domain.User;
import com.theme.dto.UserResponseDto;
import com.theme.repository.UserRepository;
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
