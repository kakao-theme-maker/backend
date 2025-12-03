package com.komentum.user.service;

import com.komentum.post.domain.Post;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.SubscriptionRepository;
import com.komentum.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserRetrieveService {

  private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final PostService postService;

  public UserRetrieveService(UserRepository userRepository,
      SubscriptionRepository subscriptionRepository,
      PostService postService) {
    this.userRepository = userRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.postService = postService;
  }

  public UserResponseDto getUserByEmail(String email) {
    User user = findUserEntity(email);
    //팔로워 수
    int followers = subscriptionRepository.countBySubscriber_UserEmail(email);
    //팔로잉 수
    int following = subscriptionRepository.countByUser_UserEmail(email);
    //업로드 수
    int uploads = postService.countPost(email);
    return UserResponseDto.from(user, followers, following, uploads);
  }

  public User findUserEntity(String email){
    return userRepository.findById(email)
        .orElseThrow(() -> new RuntimeException("user not found"));
  }
}
