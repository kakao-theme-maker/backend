package com.komentum.user.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.global.dto.CustomResponse;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserBirthUpdateDto;
import com.komentum.user.dto.UserGenderUpdateDto;
import com.komentum.user.dto.UserNameUpdateDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerTest {

  String email = "admin1@gmail.com";
  String password = "qwer123!";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserDataGenerator userDataGenerator;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PostRepository postRepository;
  @MockitoBean
  private FileManager fileManager;

  User user;
  @Autowired
  private MockMvcUtils mockMvcUtils;

  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    userDataGenerator.generateRetrieveTestUser(email, password);
    addPostForUser(email);
    user = userRepository.findByUserEmail(email).orElseThrow();
  }

  @AfterEach
  void tearDown() {
    userDataGenerator.deleteAllUsers();
  }

  // upload 확인용
  private void addPostForUser(String email) {
    User user = userRepository.findByUserEmail(email)
        .orElseThrow();
    Post post = Post.builder()
        .title("test")
        .content("test-content")
        .user(user)
        .postType(PostType.THEME_BOARD)
        .build();

    postRepository.save(post);
  }

  // Map 형태의 파라미터를 MultiValueMap으로 변환
  private LinkedMultiValueMap<String, String> params(Map<String, String> map) {
    LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.setAll(map);
    return params;
  }

  @Test
  @DisplayName("유저 조회")
  void inquiryUserTest() throws Exception {
    //given
    UserResponseDto userResponseDto =
        UserResponseDto.builder()
            .userEmail(user.getUserEmail())
            .name(user.getName())
            .gender(user.getGender())
            .birth(user.getBirth())
            .profileImage(user.getProfileImgUrl())
            .profileImageName(user.getProfileImgName())
            .publicUserId(user.getPublicUserId())
            .uploads(1)
            .followers(0)
            .following(0)
            .build();

    //when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<Void, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users")
            .httpMethod(HttpMethod.GET)
            .params(params(Map.of("userPublicID", user.getPublicUserId())))
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    //then

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(userResponseDto);
  }

  @Test
  @DisplayName("현재 인증된 사용자 정보 조회")
  void retrieveCurrentUser_success() throws Exception {
    // given
    User targetUser = userDataGenerator.generateTestUser(UUID.randomUUID() + "@test.com");
    // when
    UserResponseDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, UserResponseDto>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(targetUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getUserEmail()).isEqualTo(targetUser.getUserEmail());
    assertThat(response.getName()).isEqualTo(targetUser.getName());
    assertThat(response.getPublicUserId()).isEqualTo(targetUser.getPublicUserId());
  }

  @Test
  @DisplayName("유저 이름 수정")
  void updateUserNameTest() throws Exception {
    // given
    String updatedUserName = "updatedName";
    UserNameUpdateDto updateDto = UserNameUpdateDto.builder()
        .name(updatedUserName)
        .build();

    // when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<UserNameUpdateDto, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/name")
            .httpMethod(HttpMethod.PATCH)
            .body(updateDto)
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    // 응답 검증
    assertThat(result.getName()).isEqualTo(updatedUserName);

    // DB 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getName()).isEqualTo(updatedUserName);
  }

  @Test
  @DisplayName("유저 프로필 이미지 수정")
  void updateUserProfileImageTest() throws Exception {
    // given
    String oldImageFileName = "old_image.png";
    user.setProfileImgName(oldImageFileName);
    user.setProfileImgUrl("https://test.com/" + oldImageFileName);
    userRepository.save(user);

    MockMultipartFile profileImage = new MockMultipartFile(
        "profile_image",
        "test-image.png",
        "image/png",
        "test image content".getBytes()
    );
    String expectedImageUrl = "https://test.com/test-image.png";

    Mockito.when(fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(expectedImageUrl);
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(expectedImageUrl);

    // when
    CustomResponse<UserResponseDto> response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/profile-image")
            .httpMethod(HttpMethod.PATCH)
            .formDataList(List.of(profileImage))
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    UserResponseDto result = response.getData();

    // 프로필 이미지 URL이 변경되었는지 검증
    assertThat(result.getProfileImage()).isEqualTo(expectedImageUrl);

    // 파일명이 저장되었는지 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getProfileImgUrl()).isEqualTo(expectedImageUrl);
    assertThat(updatedUser.getProfileImgName()).isNotNull();
    assertThat(updatedUser.getProfileImgName()).endsWith(".png");
    assertThat(updatedUser.getProfileImgName()).isNotEqualTo(oldImageFileName);

    // FileManager 호출 검증
    Mockito.verify(fileManager).uploadFile(any(byte[].class), contains("User"));
    Mockito.verify(fileManager, Mockito.times(1)).deleteFile(oldImageFileName);
    Mockito.verify(fileManager).resolveFilePath(updatedUser.getProfileImgName());
  }

  @Test
  @DisplayName("유저 성별 수정")
  void updateUserGenderTest() throws Exception {
    // given
    Gender updatedGender = Gender.male;
    UserGenderUpdateDto updateDto = UserGenderUpdateDto.builder()
        .gender(updatedGender)
        .build();

    // when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<UserGenderUpdateDto, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/gender")
            .httpMethod(HttpMethod.PATCH)
            .body(updateDto)
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    // 응답 검증
    assertThat(result.getGender()).isEqualTo(updatedGender);

    // DB 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getGender()).isEqualTo(updatedGender);
  }

  @Test
  @DisplayName("유저 생년월일 수정")
  void updateUserBirthTest() throws Exception {
    // given
    LocalDate updatedBirth = LocalDate.of(2000, 1, 1);
    UserBirthUpdateDto updateDto = UserBirthUpdateDto.builder()
        .birth(updatedBirth)
        .build();

    // when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<UserBirthUpdateDto, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/birth")
            .httpMethod(HttpMethod.PATCH)
            .body(updateDto)
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    // 응답 검증
    assertThat(result.getBirth()).isEqualTo(updatedBirth);

    // DB 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getBirth()).isEqualTo(updatedBirth);
  }
}
