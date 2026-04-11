package com.komentum.user.service;

import com.komentum.global.utils.FileManager;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserBirthUpdateDto;
import com.komentum.user.dto.UserGenderUpdateDto;
import com.komentum.user.dto.UserNameUpdateDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.SubscriptionRepository;
import com.komentum.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class UserService implements UserEntityFinder {

  private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final PostService postService;
  private final BoardManagementHelper boardManagementHelper;
  private final FileManager fileManager;

  public UserService(UserRepository userRepository,
      SubscriptionRepository subscriptionRepository,
      PostService postService,
      BoardManagementHelper boardManagementHelper,
      FileManager fileManager) {
    this.userRepository = userRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.postService = postService;
    this.boardManagementHelper = boardManagementHelper;
    this.fileManager = fileManager;
  }

  public UserResponseDto getUserByPublicId(String publicUserId) {
    User user = findUserEntityByPublicId(publicUserId);
    //팔로워 수
    int followers = subscriptionRepository.countBySubscriber_publicUserId(publicUserId);
    //팔로잉 수
    int following = subscriptionRepository.countByUser_publicUserId(publicUserId);
    //업로드 수
    int uploads = postService.countPost(publicUserId);
    // 프로필 이미지 URL 변환
    String profileImageUrl = resolveProfileImageUrl(user.getProfileImg());
    return UserResponseDto.from(user, followers, following, uploads, profileImageUrl);
  }

  public User findUserEntity(String PublicUserId) {
    return userRepository.findByPublicUserId(PublicUserId)
        .orElseThrow(() -> new RuntimeException("user not found"));
  }

  public User findUserEntityByPublicId(String PublicUserId) {
    return userRepository.findByPublicUserId(PublicUserId)
        .orElseThrow(() -> new RuntimeException("user not found"));
  }

  // 유저 이름 수정
  @Transactional
  public UserResponseDto updateUserName(String publicUserId, UserNameUpdateDto updateDto) {
    User user = findUserEntityByPublicId(publicUserId);
    user.setName(updateDto.getName());
    return getUserByPublicId(publicUserId);
  }

  // 유저 프로필 이미지 수정
  @Transactional
  public UserResponseDto updateUserProfileImage(String publicUserId, MultipartFile profileImage) {
    User user = findUserEntityByPublicId(publicUserId);
    String oldImageFileName = user.getProfileImg();

    // 새 이미지 업로드
    String newImageFileName = boardManagementHelper.savePreviewImageIfPresent(User.class,
        profileImage);

    try {
      user.setProfileImg(newImageFileName);

      // 기존 이미지 삭제
      if (oldImageFileName != null && !oldImageFileName.startsWith("http")) {
        boardManagementHelper.deleteFileSilently(oldImageFileName, "프로필 이미지 수정 시 기존 이미지 삭제 실패");
      }
    } catch (Exception e) {
      // DB 업데이트 실패 시 새로 업로드한 파일 삭제
      boardManagementHelper.deleteFileSilently(newImageFileName, "프로필 이미지 수정 실패로 인한 파일 롤백 실패");
      throw e;
    }

    return getUserByPublicId(publicUserId);
  }

  // 유저 성별 수정
  @Transactional
  public UserResponseDto updateUserGender(String publicUserId, UserGenderUpdateDto updateDto) {
    User user = findUserEntityByPublicId(publicUserId);
    user.setGender(updateDto.getGender());
    return getUserByPublicId(publicUserId);
  }

  // 유저 생년월일 수정
  @Transactional
  public UserResponseDto updateUserBirth(String publicUserId, UserBirthUpdateDto updateDto) {
    User user = findUserEntityByPublicId(publicUserId);
    user.setBirth(updateDto.getBirth());
    return getUserByPublicId(publicUserId);
  }

  /**
   * 프로필 이미지 URL 변환
   *
   * @param profileImg
   * @return
   */
  private String resolveProfileImageUrl(String profileImg) {
    if (profileImg == null) {
      return null;
    }
    if (profileImg.startsWith("http://") || profileImg.startsWith("https://")) {
      return profileImg;
    }
    return fileManager.resolveFilePath(profileImg);
  }
}
