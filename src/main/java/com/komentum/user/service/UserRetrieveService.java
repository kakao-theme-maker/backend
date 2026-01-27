package com.komentum.user.service;

import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.dto.UserUpdateDto;
import com.komentum.user.repository.SubscriptionRepository;
import com.komentum.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserRetrieveService implements UserEntityFinder {

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

  public UserResponseDto getUserByPublicId(String publicUserId) {
    User user = findUserEntityByPublicId(publicUserId);
    //팔로워 수
    int followers = subscriptionRepository.countBySubscriber_publicUserId(publicUserId);
    //팔로잉 수
    int following = subscriptionRepository.countByUser_publicUserId(publicUserId);
    //업로드 수
    int uploads = postService.countPost(publicUserId);
    return UserResponseDto.from(user, followers, following, uploads);
  }

//  public User findUserEntity(String email){
//    return userRepository.findByUserEmail(email)
//        .orElseThrow(() -> new RuntimeException("user not found"));
//  }

  public User findUserEntity(String PublicUserId){
    return userRepository.findByPublicUserId(PublicUserId)
        .orElseThrow(() -> new RuntimeException("user not found"));
  }

  public User findUserEntityByPublicId(String PublicUserId){
    return userRepository.findByPublicUserId(PublicUserId)
        .orElseThrow(() -> new RuntimeException("user not found"));
  }

  // 유저 정보 수정
  @Transactional
  public UserResponseDto updateUser(String publicUserId, UserUpdateDto updateDto){
    User user = findUserEntityByPublicId(publicUserId);
    if(updateDto.getName() != null ){
      user.setName(updateDto.getName());
    }
    if(updateDto.getProfileImage()!= null){
      user.setProfileImg(updateDto.getProfileImage());
    }
    if(updateDto.getGender() != null){
      user.setGender(updateDto.getGender());
    }
    if (updateDto.getBirth() != null){
      user.setBirth(updateDto.getBirth());
    }
    //팔로워 수
    int followers = subscriptionRepository.countBySubscriber_publicUserId(publicUserId);
    //팔로잉 수
    int following = subscriptionRepository.countByUser_publicUserId(publicUserId);
    //업로드 수
    int uploads = postService.countPost(publicUserId);
    return UserResponseDto.from(user, followers, following, uploads);
  }
}
