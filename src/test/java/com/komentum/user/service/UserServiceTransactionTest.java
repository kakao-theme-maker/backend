package com.komentum.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.komentum.global.utils.FileManager;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.UserRepository;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@EnableTestProfile
class UserServiceTransactionTest {

  private static final String OLD_IMAGE_FILE_NAME = "old-profile.png";
  private static final String COMMIT_FAILURE_MESSAGE = "forced commit failure";

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Autowired
  private FileManager fileManager;

  @Autowired
  private PlatformTransactionManager transactionManager;

  private TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    reset(fileManager);
    transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @AfterEach
  void tearDown() {
    userDataGenerator.deleteAllUsers();
    reset(fileManager);
  }

  @Test
  @DisplayName("커밋 시점에 실패하면 기존 프로필 이미지는 유지되고 새 이미지만 삭제된다")
  void updateUserProfileImage_rollsBackUploadedFileWhenCommitFails() {
    // given
    User user = createUserWithProfileImage(OLD_IMAGE_FILE_NAME);
    MockMultipartFile profileImage = MockMultipartFileUtils.generateImageFormData(
        "profile_image",
        MockMultipartFileUtils.ImageExtension.PNG
    );
    AtomicReference<UserResponseDto> responseHolder = new AtomicReference<>();
    stubFileManager();

    // when
    assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
      registerCommitFailure();
      responseHolder.set(userService.updateUserProfileImage(user.getPublicUserId(), profileImage));
    }))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(COMMIT_FAILURE_MESSAGE);

    // then
    UserResponseDto response = responseHolder.get();
    assertThat(response).isNotNull();
    assertThat(response.getProfileImageName()).isNotBlank();
    assertThat(response.getProfileImageName()).isNotEqualTo(OLD_IMAGE_FILE_NAME);
    assertThat(response.getProfileImage()).isEqualTo(buildImageUrl(response.getProfileImageName()));

    User reloadedUser = userRepository.findByPublicUserId(user.getPublicUserId()).orElseThrow();
    assertThat(reloadedUser.getProfileImgName()).isEqualTo(OLD_IMAGE_FILE_NAME);
    assertThat(reloadedUser.getProfileImgUrl()).isEqualTo(buildImageUrl(OLD_IMAGE_FILE_NAME));

    verify(fileManager).deleteFile(response.getProfileImageName());
    verify(fileManager, never()).deleteFile(OLD_IMAGE_FILE_NAME);
  }

  private User createUserWithProfileImage(String imageFileName) {
    User user = userDataGenerator.generateTestUser(UUID.randomUUID() + "@test.com");
    user.setProfileImgName(imageFileName);
    user.setProfileImgUrl(buildImageUrl(imageFileName));
    return userRepository.save(user);
  }

  private void stubFileManager() {
    given(fileManager.uploadFile(any(byte[].class), anyString()))
        .willAnswer(invocation -> buildImageUrl(invocation.getArgument(1, String.class)));
    given(fileManager.resolveFilePath(anyString()))
        .willAnswer(invocation -> buildImageUrl(invocation.getArgument(0, String.class)));
  }

  private void registerCommitFailure() {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void beforeCommit(boolean readOnly) {
        throw new IllegalStateException(COMMIT_FAILURE_MESSAGE);
      }
    });
  }

  private String buildImageUrl(String fileName) {
    return "https://cdn.example.com/" + fileName;
  }
}
